package org.thoughtcrime.securesms;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.thoughtcrime.securesms.components.CornerMaskingView;
import org.thoughtcrime.securesms.components.CornerMaskingView.CornerSpec;
import org.thoughtcrime.securesms.contactshare.Contact;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.MmsMessageRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.DateUtils;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface BindableConversationItem extends Unbindable {
  void bind(@NonNull MessageRecord           messageRecord,
            @NonNull Optional<MessageRecord> previousMessageRecord,
            @NonNull Optional<MessageRecord> nextMessageRecord,
            @NonNull GlideRequests           glideRequests,
            @NonNull Locale                  locale,
            @NonNull Set<MessageRecord>      batchSelected,
            @NonNull Recipient               recipients,
                     boolean                 pulseHighlight);

  MessageRecord getMessageRecord();

  void setEventListener(@Nullable EventListener listener);

  static int getCornerBackgroundRes(@NonNull MessageRecord current, @NonNull Optional<MessageRecord> previous, @NonNull Optional<MessageRecord> next, boolean isGroupThread) {
    if (isSingularMessage(current, previous, next, isGroupThread)) {
      return current.isOutgoing() ? R.drawable.message_bubble_background_sent_alone
                                  : R.drawable.message_bubble_background_received_alone;
    } else if (isStartOfMessageCluster(current, previous, isGroupThread)) {
      return current.isOutgoing() ? R.drawable.message_bubble_background_sent_start
                                  : R.drawable.message_bubble_background_received_start;
    } else if (isEndOfMessageCluster(current, next, isGroupThread)) {
      return current.isOutgoing() ? R.drawable.message_bubble_background_sent_end
                                  : R.drawable.message_bubble_background_received_end;
    } else {
      return current.isOutgoing() ? R.drawable.message_bubble_background_sent_middle
                                  : R.drawable.message_bubble_background_received_middle;
    }
  }

  static CornerSpec getCornerSpec(@NonNull MessageRecord current, @NonNull Optional<MessageRecord> previous, @NonNull Optional<MessageRecord> next, boolean isGroupThread) {
    if (isSingularMessage(current, previous, next, isGroupThread)) {
      return current.isOutgoing() ? BubbleCornerSpec.OUTGOING_ALONE : BubbleCornerSpec.INCOMING_ALONE;
    } else if (isStartOfMessageCluster(current, previous, isGroupThread)) {
      return current.isOutgoing() ? BubbleCornerSpec.OUTGOING_START : BubbleCornerSpec.INCOMING_START;
    } else if (isEndOfMessageCluster(current, next, isGroupThread)) {
      return current.isOutgoing() ? BubbleCornerSpec.OUTGOING_END : BubbleCornerSpec.INCOMING_END;
    } else {
      return current.isOutgoing() ? BubbleCornerSpec.OUTGOING_MIDDLE : BubbleCornerSpec.IMCOMING_MIDDLE;
    }
  }

  static boolean isStartOfMessageCluster(@NonNull MessageRecord current, @NonNull Optional<MessageRecord> previous, boolean isGroupThread) {
    if (isGroupThread) {
      return !previous.isPresent() || previous.get().isUpdate() || !DateUtils.isSameDay(current.getTimestamp(), previous.get().getTimestamp()) ||
             !current.getRecipient().getAddress().equals(previous.get().getRecipient().getAddress());
    } else {
      return !previous.isPresent() || previous.get().isUpdate() || !DateUtils.isSameDay(current.getTimestamp(), previous.get().getTimestamp()) ||
             current.isOutgoing() != previous.get().isOutgoing();
    }
  }

  static boolean isEndOfMessageCluster(@NonNull MessageRecord current, @NonNull Optional<MessageRecord> next, boolean isGroupThread) {
    if (isGroupThread) {
      return !next.isPresent() || next.get().isUpdate() || !DateUtils.isSameDay(current.getTimestamp(), next.get().getTimestamp()) ||
             !current.getRecipient().getAddress().equals(next.get().getRecipient().getAddress());
    } else {
      return !next.isPresent() || next.get().isUpdate() || !DateUtils.isSameDay(current.getTimestamp(), next.get().getTimestamp()) ||
             current.isOutgoing() != next.get().isOutgoing();
    }
  }

  static boolean isSingularMessage(@NonNull MessageRecord current, @NonNull Optional<MessageRecord> previous, @NonNull Optional<MessageRecord> next, boolean isGroupThread) {
    return isStartOfMessageCluster(current, previous, isGroupThread) && isEndOfMessageCluster(current, next, isGroupThread);
  }

  static int getMessageSpacing(@NonNull Context context, @NonNull MessageRecord current, @NonNull Optional<MessageRecord> next) {
    if (next.isPresent()) {
      boolean recipientsMatch = current.getRecipient().getAddress().equals(next.get().getRecipient().getAddress());
      boolean outgoingMatch   = current.isOutgoing() == next.get().isOutgoing();

      if (!recipientsMatch || !outgoingMatch) {
        return readDimen(context, R.dimen.conversation_vertical_message_spacing_default);
      }
    }
    return readDimen(context, R.dimen.conversation_vertical_message_spacing_collapse);
  }

  static int readDimen(@NonNull Context context, @DimenRes int dimenId) {
    return context.getResources().getDimensionPixelOffset(dimenId);
  }

  interface EventListener {
    void onQuoteClicked(MmsMessageRecord messageRecord);
    void onSharedContactDetailsClicked(@NonNull Contact contact, @NonNull View avatarTransitionView);
    void onAddToContactsClicked(@NonNull Contact contact);
    void onMessageSharedContactClicked(@NonNull List<Recipient> choices);
    void onInviteSharedContactClicked(@NonNull List<Recipient> choices);
  }

  enum BubbleCornerSpec implements CornerSpec {
    OUTGOING_ALONE(R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius),
    OUTGOING_START(R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_collapse_radius, R.dimen.message_corner_radius),
    OUTGOING_MIDDLE(R.dimen.message_corner_radius, R.dimen.message_corner_collapse_radius, R.dimen.message_corner_collapse_radius, R.dimen.message_corner_radius),
    OUTGOING_END(R.dimen.message_corner_radius, R.dimen.message_corner_collapse_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius),

    INCOMING_ALONE(R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius),
    INCOMING_START(R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_collapse_radius),
    IMCOMING_MIDDLE(R.dimen.message_corner_collapse_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_collapse_radius),
    INCOMING_END(R.dimen.message_corner_collapse_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius, R.dimen.message_corner_radius);

    private final int topLeft;
    private final int topRight;
    private final int bottomRight;
    private final int bottomLeft;

    BubbleCornerSpec(@DimenRes int topLeft, @DimenRes int topRight, @DimenRes int bottomRight, @DimenRes int bottomLeft) {
      this.topLeft     = topLeft;
      this.topRight    = topRight;
      this.bottomRight = bottomRight;
      this.bottomLeft  = bottomLeft;
    }

    @Override
    public int getTopLeft(@NonNull Context context) {
      return BindableConversationItem.readDimen(context, topLeft);
    }

    @Override
    public int getTopRight(@NonNull Context context) {
      return BindableConversationItem.readDimen(context, topRight);
    }

    @Override
    public int getBottomRight(@NonNull Context context) {
      return BindableConversationItem.readDimen(context, bottomRight);
    }

    @Override
    public int getBottomLeft(@NonNull Context context) {
      return BindableConversationItem.readDimen(context, bottomLeft);
    }
  }
}
