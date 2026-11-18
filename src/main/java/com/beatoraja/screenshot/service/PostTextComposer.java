package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayScore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds post text from the user's settings: one line per screenshot, joined per post, and
 * for X fitted to the length limit (titles shortened, or the lamp-grouped form for many images).
 */
public final class PostTextComposer {

    /** What a screenshot's line needs beyond the entry itself: its post notation and matched score. */
    public record PostContext(String notation, PlayScore score) {
        public static final PostContext NONE = new PostContext("", null);
    }

    /** X post texts, one per batch, and how many titles were shortened to fit. */
    public record TweetTexts(List<String> texts, int abbreviatedCount) {
    }

    /**
     * Titles are never auto-shortened past this many weighted units. Kept generous
     * (~15 Japanese characters) since the notation/comment portion of a caption is
     * typically at most ~100 characters, not the reason a post goes over the limit, and
     * because the confirmation dialog shown before posting is a manual-edit fallback for
     * whatever this can't fix automatically - there's no need to crush titles further.
     */
    private static final int TITLE_MIN_WEIGHTED_LENGTH = 30;

    private final AppConfig config;
    private final Function<ScreenshotEntry, PostContext> contextResolver;

    public PostTextComposer(AppConfig config, Function<ScreenshotEntry, PostContext> contextResolver) {
        this.config = config;
        this.contextResolver = contextResolver;
    }

    /** The line for one screenshot in the user's post format. */
    public String message(ScreenshotEntry entry) {
        return message(entry, Integer.MAX_VALUE);
    }

    private String message(ScreenshotEntry entry, int maxTitleWeightedLength) {
        PostContext context = contextResolver.apply(entry);
        return TweetTextGenerator.generate(entry, context.notation(), context.score(), config.getPostTextFormat(),
                maxTitleWeightedLength, config.getClearLampLabels());
    }

    /** Each screenshot's line, keyed by file name, in the given order. */
    public Map<String, String> messagesByFile(List<ScreenshotEntry> entries) {
        Map<String, String> messages = new LinkedHashMap<>();
        for (ScreenshotEntry entry : entries) {
            messages.put(entry.getFileName(), message(entry));
        }
        return messages;
    }

    /** The lines of {@code entries} joined into one post. */
    public String merged(List<ScreenshotEntry> entries) {
        return join(entries, messagesByFile(entries));
    }

    public static String join(List<ScreenshotEntry> batch, Map<String, String> messagesByFile) {
        return batch.stream().map(e -> messagesByFile.get(e.getFileName())).collect(Collectors.joining("\n"));
    }

    /** One text per batch, each within X's length limit as far as shortening titles allows. */
    public TweetTexts tweetTexts(List<List<ScreenshotEntry>> batches) {
        List<String> texts = new ArrayList<>();
        int abbreviated = 0;
        for (List<ScreenshotEntry> batch : batches) {
            if (LampGroupedTweetText.shouldUse(batch.size())) {
                texts.add(lampGrouped(batch));
                continue;
            }
            Map<String, String> messages = messagesByFile(batch);
            abbreviated += abbreviateIfOverLimit(batch, messages);
            texts.add(join(batch, messages));
        }
        return new TweetTexts(texts, abbreviated);
    }

    private String lampGrouped(List<ScreenshotEntry> batch) {
        List<LampGroupedTweetText.Item> items = new ArrayList<>();
        for (ScreenshotEntry entry : batch) {
            items.add(new LampGroupedTweetText.Item(entry.getClearType(), contextResolver.apply(entry).notation(),
                    entry.getTitle()));
        }
        return LampGroupedTweetText.generate(items, config.getClearLampShortLabels(),
                config.getClearLampGroupedVisibility());
    }

    /**
     * If the batch's joined text is over the limit, shortens titles - longest first - until it
     * fits or no more can be shortened. Returns how many titles it shortened.
     */
    private int abbreviateIfOverLimit(List<ScreenshotEntry> batch, Map<String, String> messages) {
        int over = TweetTextLimits.weightedLength(join(batch, messages)) - TweetTextLimits.WEIGHTED_LIMIT;
        if (over <= 0) {
            return 0;
        }
        List<ScreenshotEntry> candidates = new ArrayList<>(batch);
        candidates.sort((a, b) -> Integer.compare(
                TweetTextLimits.weightedLength(b.getTitle()), TweetTextLimits.weightedLength(a.getTitle())));

        int abbreviated = 0;
        for (ScreenshotEntry entry : candidates) {
            if (over <= 0) {
                break;
            }
            int titleWeighted = TweetTextLimits.weightedLength(entry.getTitle());
            int targetTitleWeighted = Math.max(TITLE_MIN_WEIGHTED_LENGTH, titleWeighted - over - 1);
            if (targetTitleWeighted >= titleWeighted) {
                continue;
            }
            messages.put(entry.getFileName(), message(entry, targetTitleWeighted));
            abbreviated++;
            over = TweetTextLimits.weightedLength(join(batch, messages)) - TweetTextLimits.WEIGHTED_LIMIT;
        }
        return abbreviated;
    }
}
