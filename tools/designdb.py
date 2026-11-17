import json
import re
import sqlite3
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAIN_DIR = "src/main/java"
TEST_DIR = "src/test/java"


class SqlBook:
    def __init__(self, path, ddl):
        self.path = Path(path)
        self.ddl = ddl
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self.conn = sqlite3.connect(self.path)
        self.conn.row_factory = sqlite3.Row
        self.conn.executescript(ddl)
        self.conn.execute("PRAGMA foreign_keys=ON")

    def _pk(self, table):
        info = self.conn.execute(f'PRAGMA table_info("{table}")').fetchall()
        return [r["name"] for r in sorted((r for r in info if r["pk"]), key=lambda r: r["pk"])], [r["name"] for r in info]

    def execute(self, sql, params=()):
        with self.conn:
            return self.conn.execute(sql, params).rowcount

    def rows(self, sql, params=()):
        return [dict(r) for r in self.conn.execute(sql, params).fetchall()]

    def tables(self):
        return [r["name"] for r in self.conn.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY rowid")]

    @staticmethod
    def _lit(v):
        if v is None:
            return "NULL"
        if isinstance(v, (int, float)):
            return repr(v)
        return "'" + str(v).replace("'", "''") + "'"

    def dump(self, path):
        out = [self.ddl.strip(), ""]
        for t in self.tables():
            pk, cols = self._pk(t)
            order = ",".join(f'"{c}"' for c in (pk or cols))
            for r in self.conn.execute(f'SELECT * FROM "{t}" ORDER BY {order}'):
                out.append(f'INSERT INTO "{t}"({",".join(chr(34) + c + chr(34) for c in cols)}) VALUES({",".join(self._lit(r[c]) for c in cols)});')
            out.append("")
        Path(path).write_text("\n".join(out), encoding="utf-8", newline="\n")

    @staticmethod
    def restore(path, sql_path):
        path = Path(path)
        if path.exists():
            path.unlink()
        c = sqlite3.connect(path)
        c.executescript(Path(sql_path).read_text(encoding="utf-8"))
        c.commit()
        c.close()

    def close(self):
        self.conn.close()


class DesignBook(SqlBook):
    """Design DB: one table per class (rows = methods) plus _class, _dependency, _test, _refactor."""

    DDL = """
CREATE TABLE IF NOT EXISTS _class(
  name TEXT PRIMARY KEY,
  module TEXT NOT NULL,
  layer TEXT NOT NULL,
  kind TEXT NOT NULL DEFAULT '通常' CHECK(kind IN('通常','基底','アダプタ')),
  responsibility TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT '設計済み' CHECK(status IN('設計済み','テスト済み','実装済み')));
CREATE TABLE IF NOT EXISTS _dependency(
  from_class TEXT NOT NULL REFERENCES _class(name),
  to_class TEXT NOT NULL REFERENCES _class(name),
  kind TEXT NOT NULL CHECK(kind IN('継承','利用','生成')),
  reason TEXT NOT NULL,
  PRIMARY KEY(from_class,to_class));
CREATE TABLE IF NOT EXISTS _test(
  class TEXT NOT NULL REFERENCES _class(name),
  name TEXT NOT NULL,
  method TEXT,
  checks TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT '未作成' CHECK(status IN('未作成','失敗確認済み','成功')),
  PRIMARY KEY(class,name));
CREATE TABLE IF NOT EXISTS _refactor(
  id INTEGER PRIMARY KEY,
  class TEXT NOT NULL REFERENCES _class(name),
  title TEXT NOT NULL,
  detail TEXT NOT NULL,
  priority TEXT NOT NULL DEFAULT '中' CHECK(priority IN('高','中','低')),
  status TEXT NOT NULL DEFAULT '候補' CHECK(status IN('候補','対応中','完了','見送り')));
"""
    KEYS = {"class": ("name",), "dependency": ("from_class", "to_class"), "method": ("class", "name"),
            "test_case": ("class", "name"), "refactor": ("id",)}
    DEFAULTS = {"class": {"kind": "通常", "status": "設計済み"}, "dependency": {}, "method": {},
                "test_case": {"method": None, "status": "未作成"}, "refactor": {"priority": "中", "status": "候補"}}
    COLUMNS = {"class": ("name", "module", "layer", "kind", "responsibility", "status"),
               "dependency": ("from_class", "to_class", "kind", "reason"),
               "test_case": ("class", "name", "method", "checks", "status"),
               "refactor": ("id", "class", "title", "detail", "priority", "status")}
    TABLES = {"class": "_class", "dependency": "_dependency", "test_case": "_test", "refactor": "_refactor"}

    def __init__(self, path, dump=True):
        super().__init__(path, self.DDL)
        self.auto = dump
        self.sql_path = self.path.with_suffix(".sql")
        self.ddl = ";\n".join(self._schema(self.snapshot())) + ";"

    def snapshot(self):
        classes = self.rows("SELECT * FROM _class ORDER BY name")
        have = set(self.tables())
        methods = []
        for c in classes:
            if c["name"] in have:
                for r in self.rows(f'SELECT method, signature, behavior FROM "{c["name"]}" ORDER BY method'):
                    methods.append({"class": c["name"], "name": r["method"], "signature": r["signature"], "behavior": r["behavior"]})
        return {"class": classes,
                "dependency": self.rows("SELECT * FROM _dependency ORDER BY from_class, to_class"),
                "method": methods,
                "test_case": self.rows("SELECT * FROM _test ORDER BY class, name"),
                "refactor": self.rows("SELECT * FROM _refactor ORDER BY id")}

    def _schema(self, snap):
        out = [s.strip() for s in self.DDL.split(";") if s.strip()]
        names = sorted(c["name"] for c in snap["class"])
        for n in names:
            cols = ["method TEXT PRIMARY KEY", "signature TEXT NOT NULL", "behavior TEXT NOT NULL"]
            for d in sorted((d for d in snap["dependency"] if d["from_class"] == n), key=lambda d: d["to_class"]):
                cols.append(f'"{d["kind"]}_{d["to_class"]}" TEXT REFERENCES "{d["to_class"]}"(method)')
            out.append(f'CREATE TABLE IF NOT EXISTS "{n}"(\n  ' + ",\n  ".join(cols) + ")")
        union = "\n  UNION ALL ".join(f"SELECT '{n}' AS class, method, signature, behavior FROM \"{n}\"" for n in names) \
            or "SELECT NULL AS class, NULL AS method, NULL AS signature, NULL AS behavior WHERE 0"
        out.append("CREATE VIEW IF NOT EXISTS v_method AS\n  SELECT c.layer AS 層, m.class AS クラス, m.method AS メソッド, m.signature AS 引数と戻り値, m.behavior AS 処理内容\n  FROM (" + union + ") m JOIN _class c ON c.name=m.class ORDER BY c.layer, m.class, m.method")
        out.append("CREATE VIEW IF NOT EXISTS v_class_detail AS\n  SELECT c.layer AS 層, c.name AS クラス, c.kind AS 種類, c.status AS 状態, c.module AS モジュール, c.responsibility AS 責務,\n    (SELECT group_concat(m.引数と戻り値 || ' … ' || m.処理内容, char(10)) FROM v_method m WHERE m.クラス=c.name) AS メソッド\n  FROM _class c ORDER BY c.layer, c.name")
        out.append("CREATE VIEW IF NOT EXISTS v_dependency AS\n  SELECT d.from_class AS 依存元, f.layer AS 依存元の層, d.to_class AS 依存先, t.layer AS 依存先の層, d.kind AS 種類, d.reason AS 理由\n  FROM _dependency d JOIN _class f ON f.name=d.from_class JOIN _class t ON t.name=d.to_class ORDER BY f.layer, d.from_class, d.to_class")
        out.append("CREATE VIEW IF NOT EXISTS v_test AS\n  SELECT t.class AS クラス, t.method AS メソッド, t.name AS テスト名, t.checks AS 確かめること, t.status AS 状態\n  FROM _test t ORDER BY t.class, t.name")
        out.append("CREATE VIEW IF NOT EXISTS v_refactor AS\n  SELECT r.id AS 番号, r.priority AS 優先度, r.status AS 状態, c.layer AS 層, r.class AS クラス, r.title AS 件名, r.detail AS 内容\n  FROM _refactor r JOIN _class c ON c.name=r.class ORDER BY CASE r.status WHEN '対応中' THEN 0 WHEN '候補' THEN 1 ELSE 2 END, CASE r.priority WHEN '高' THEN 0 WHEN '中' THEN 1 ELSE 2 END, r.id")
        out.append("CREATE VIEW IF NOT EXISTS v_progress AS\n  SELECT status AS 状態, count(*) AS クラス数 FROM _class GROUP BY status ORDER BY status")
        return out

    def _rebuild(self, snap):
        names = {c["name"] for c in snap["class"]}
        for d in snap["dependency"]:
            if d["from_class"] not in names or d["to_class"] not in names:
                raise sqlite3.IntegrityError(f"依存 {d['from_class']} -> {d['to_class']}: 登録されていないクラスがあります")
        for key in ("method", "test_case", "refactor"):
            for r in snap[key]:
                if r["class"] not in names:
                    raise sqlite3.IntegrityError(f"{key} {r['class']}: クラス {r['class']} は登録されていません")
        stmts = self._schema(snap)
        c = self.conn
        c.commit()
        c.execute("PRAGMA foreign_keys=OFF")
        c.execute("BEGIN")
        try:
            old = c.execute("SELECT type, name FROM sqlite_master WHERE type IN('view','table') AND name NOT LIKE 'sqlite_%' ORDER BY type DESC").fetchall()
            for r in old:
                c.execute(f'DROP {r["type"].upper()} IF EXISTS "{r["name"]}"')
            for s in stmts:
                c.execute(s)
            for key, table in self.TABLES.items():
                cols = self.COLUMNS[key]
                for r in snap[key]:
                    c.execute(f"INSERT INTO {table}({','.join(cols)}) VALUES({','.join('?' * len(cols))})", [r[k] for k in cols])
            for r in snap["method"]:
                c.execute(f'INSERT INTO "{r["class"]}"(method,signature,behavior) VALUES(?,?,?)', [r["name"], r["signature"], r["behavior"]])
            c.execute("COMMIT")
        except Exception:
            c.execute("ROLLBACK")
            raise
        finally:
            c.execute("PRAGMA foreign_keys=ON")
        self.ddl = ";\n".join(stmts) + ";"

    def save(self):
        if self.auto:
            self.dump(self.sql_path)

    def apply(self, data):
        snap = self.snapshot()
        for key, pk in self.KEYS.items():
            idx = {tuple(r[c] for c in pk): r for r in snap[key]}
            for row in data.get(key, []):
                if key == "refactor" and "id" not in row:
                    row = {**row, "id": max((k[0] for k in idx), default=0) + 1}
                k = tuple(row[c] for c in pk)
                idx[k] = {**self.DEFAULTS[key], **idx.get(k, {}), **row}
            for row in data.get("remove_" + key, []):
                idx.pop(tuple(row[c] for c in pk), None)
            snap[key] = [idx[k] for k in sorted(idx)]
        self._rebuild(snap)
        self.save()

    def set_status(self, name, status):
        self.execute("UPDATE _class SET status=? WHERE name=?", (status, name))
        self.save()

    def set_test_status(self, cls, name, status):
        if name == "*":
            self.execute("UPDATE _test SET status=? WHERE class=?", (status, cls))
        else:
            self.execute("UPDATE _test SET status=? WHERE class=? AND name=?", (status, cls, name))
        self.save()

    def set_refactor_status(self, ident, status):
        self.execute("UPDATE _refactor SET status=? WHERE id=?", (status, int(ident)))
        self.save()


class JavaSource:
    """Reads the top-level types of the Java sources: module, non-private methods and the classes they mention."""

    TYPE_DECL = re.compile(r"^(?:public\s+|abstract\s+|final\s+|sealed\s+|non-sealed\s+)*(class|interface|enum|record)\s+(\w+)", re.M)
    MEMBER_INDENT = "    "
    METHOD_START = re.compile(r"^    (?=\S)(?!private\b)(?!return\b)(?!new\b)(?!throw\b)((?:(?:public|protected|static|final|abstract|synchronized|default|native) +)*)(<[^>\n]+> +)?([\w.$<>\[\],? ]+?) +(\w+) *\(", re.M)

    def __init__(self, root):
        self.root = Path(root)

    @staticmethod
    def strip(text):
        text = re.sub(r"/\*.*?\*/", lambda m: "\n" * m.group(0).count("\n"), text, flags=re.S)
        text = re.sub(r"//[^\n]*", "", text)
        text = re.sub(r'"""(.*?)"""', lambda m: '""' + "\n" * m.group(0).count("\n"), text, flags=re.S)
        text = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', text)
        text = re.sub(r"'(?:\\.|[^'\\\n])'", "''", text)
        return text

    def types(self, base):
        out = {}
        for f in sorted((self.root / base).rglob("*.java")):
            rel = f.relative_to(self.root).as_posix()
            text = self.strip(f.read_text(encoding="utf-8"))
            m = self.TYPE_DECL.search(text)
            if not m:
                continue
            out[m.group(2)] = {"module": rel, "kind": m.group(1), "text": text, "decl_start": m.start()}
        return out

    def _signature(self, text, start):
        depth, i = 0, text.index("(", start)
        while i < len(text):
            ch = text[i]
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        return re.sub(r"\s+", " ", text[start:i + 1].strip()).replace("( ", "(").replace(" )", ")")

    def methods(self, name, info):
        """Non-private methods declared directly in the top-level type (overloads share one entry)."""
        text = info["text"]
        modifiers = re.compile(r"^(?:public|protected|static|final|abstract|synchronized|default|native)\s+")
        found = {}
        for m in self.METHOD_START.finditer(text):
            method = m.group(4)
            if method == name or m.group(3).strip() in ("else", "case"):
                continue
            sig = self._signature(text, m.start())
            while modifiers.match(sig):
                sig = modifiers.sub("", sig)
            found.setdefault(method, []).append(sig)
        return {k: "\n".join(v) for k, v in found.items()}

    def references(self, name, info, known):
        text = info["text"][info["decl_start"]:]
        header = text[:text.index("{")] if "{" in text else text
        inherit = set()
        hm = re.search(r"\b(?:extends|implements)\b(.*)$", header, re.S)
        if hm:
            inherit = {t for t in re.findall(r"\b([A-Z]\w*)\b", hm.group(1)) if t in known}
        created = set(re.findall(r"\bnew\s+([A-Z]\w*)\b", text)) | set(re.findall(r"\b([A-Z]\w*)::new\b", text))
        used = set(re.findall(r"\b([A-Z]\w*)\b", text))
        out = {}
        for t in (used & known) - {name}:
            out[t] = "継承" if t in inherit else "生成" if t in created else "利用"
        return out

    def tests(self):
        out = []
        for f in sorted((self.root / TEST_DIR).rglob("*.java")):
            text = self.strip(f.read_text(encoding="utf-8"))
            for m in re.finditer(r"@Test\b[^;{]*?\bvoid\s+(\w+)\s*\(", text, re.S):
                out.append((f.stem, m.group(1)))
        return out


class DesignAudit:
    """Differences between the design DB and the code."""

    UPPER = {"model", "parser", "player", "table", "config", "db"}

    def __init__(self, book, root):
        self.book = book
        self.src = JavaSource(root)

    def problems(self):
        out = []
        code = self.src.types(MAIN_DIR)
        snap = self.book.snapshot()
        db = {r["name"]: r for r in snap["class"]}
        dbm, dbd = {}, {}
        for r in snap["method"]:
            dbm.setdefault(r["class"], set()).add(r["name"])
        for r in snap["dependency"]:
            dbd.setdefault(r["from_class"], {})[r["to_class"]] = r["kind"]
        known = set(code)
        for name, info in sorted(code.items()):
            if name not in db:
                out.append(f"設計DBに無いクラス: {name} ({info['module']})")
                continue
            if db[name]["module"] != info["module"]:
                out.append(f"モジュールのずれ: {name} は設計DBでは {db[name]['module']}、コードでは {info['module']}")
            methods = set(self.src.methods(name, info))
            for m in sorted(methods - dbm.get(name, set())):
                out.append(f"設計DBに無いメソッド: {name}.{m}")
            for m in sorted(dbm.get(name, set()) - methods):
                out.append(f"コードに無いメソッド: {name}.{m}")
            deps = self.src.references(name, info, known)
            for t in sorted(set(deps) - set(dbd.get(name, {}))):
                out.append(f"設計DBに無い依存: {name} -> {t} ({deps[t]})")
            for t in sorted(set(dbd.get(name, {})) - set(deps)):
                out.append(f"コードに無い依存: {name} -> {t}")
        for name, r in sorted(db.items()):
            if name not in code and r["status"] == "実装済み":
                out.append(f"実装済みなのにコードに無いクラス: {name}")
        for a, targets in sorted(dbd.items()):
            for b in sorted(targets):
                if a in db and b in db and db[a]["layer"] in self.UPPER and db[b]["kind"] == "アダプタ":
                    out.append(f"層の向きの違反: {a} ({db[a]['layer']}) がアダプタ {b} に依存している")
        tests = {n for _, n in self.src.tests()}
        dbt = {r["name"]: r["status"] for r in snap["test_case"]}
        for t in sorted(tests - set(dbt)):
            out.append(f"設計DBに無いテスト: {t}")
        for t in sorted(n for n, s in dbt.items() if s != "未作成" and n not in tests):
            out.append(f"コードに無いテスト: {t}")
        return out

    def scaffold(self):
        """JSON for whatever the code has and the DB lacks, with the text fields left as TODO."""
        code = self.src.types(MAIN_DIR)
        snap = self.book.snapshot()
        db = {r["name"] for r in snap["class"]}
        dbm = {(r["class"], r["name"]) for r in snap["method"]}
        dbd = {(r["from_class"], r["to_class"]) for r in snap["dependency"]}
        dbt = {r["name"] for r in snap["test_case"]}
        out = {"class": [], "method": [], "dependency": [], "test_case": []}
        for name, info in sorted(code.items()):
            if name not in db:
                layer = info["module"][len(MAIN_DIR) + 1:].rsplit("/", 1)[0].replace("com/beatoraja/screenshot", "").strip("/").replace("/", ".") or "app"
                out["class"].append({"name": name, "module": info["module"], "layer": layer, "kind": "通常",
                                     "responsibility": "TODO", "status": "実装済み"})
            for m, sig in sorted(self.src.methods(name, info).items()):
                if (name, m) not in dbm:
                    out["method"].append({"class": name, "name": m, "signature": sig, "behavior": "TODO"})
            for t, kind in sorted(self.src.references(name, info, set(code)).items()):
                if (name, t) not in dbd:
                    out["dependency"].append({"from_class": name, "to_class": t, "kind": kind, "reason": "TODO"})
        for test_file, t in self.src.tests():
            if t not in dbt:
                out["test_case"].append({"class": test_file[:-4] if test_file.endswith("Test") else test_file,
                                         "name": t, "method": None, "checks": "TODO", "status": "成功"})
        return out


class Cli:
    USAGE = ("python tools/designdb.py apply <json|-> | status <クラス>... <状態> | test-status <クラス> <テスト名|*> <状態>"
             " | refactor-status <番号> <状態> | audit | scaffold | dump | restore")

    @staticmethod
    def main(argv, path=None):
        path = Path(path) if path else ROOT / "design" / "design.db"
        if not argv:
            print(Cli.USAGE)
            return 2
        cmd = argv[0]
        if cmd == "restore":
            SqlBook.restore(path, path.with_suffix(".sql"))
            return 0
        d = DesignBook(path)
        try:
            if cmd == "apply":
                text = sys.stdin.read() if argv[1] == "-" else Path(argv[1]).read_text(encoding="utf-8-sig")
                d.apply(json.loads(text))
            elif cmd == "status":
                for n in argv[1:-1]:
                    d.set_status(n, argv[-1])
            elif cmd == "test-status":
                d.set_test_status(argv[1], argv[2], argv[3])
            elif cmd == "refactor-status":
                d.set_refactor_status(argv[1], argv[2])
            elif cmd == "dump":
                d.dump(d.sql_path)
            elif cmd == "audit":
                ps = DesignAudit(d, ROOT).problems()
                print("\n".join(ps) if ps else "問題なし")
                return 1 if ps else 0
            elif cmd == "scaffold":
                sys.stdout.reconfigure(encoding="utf-8")
                print(json.dumps(DesignAudit(d, ROOT).scaffold(), ensure_ascii=False, indent=1))
            else:
                print(Cli.USAGE)
                return 2
        finally:
            d.close()
        return 0


if __name__ == "__main__":
    sys.exit(Cli.main(sys.argv[1:]))
