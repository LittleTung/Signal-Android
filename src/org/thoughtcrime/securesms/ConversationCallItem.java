package org.thoughtcrime.securesms;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.DateUtils;
import org.thoughtcrime.securesms.util.ThemeUtil;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.Locale;
import java.util.Set;

public class ConversationCallItem extends RelativeLayout implements BindableConversationItem {

  private View      bubble;
  private ImageView callIconView;
  private TextView  callTextView;
  private TextView  dateView;

  private MessageRecord messageRecord;

  public ConversationCallItem(Context context) {
    super(context);
  }

  public ConversationCallItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ConversationCallItem(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bubble       = findViewById(R.id.call_bubble);
    callIconView = findViewById(R.id.call_icon);
    callTextView = findViewById(R.id.call_text);
    dateView     = findViewById(R.id.call_date);
  }

  @Override
  public void bind(@NonNull MessageRecord           messageRecord,
                   @NonNull Optional<MessageRecord> previousMessageRecord,
                   @NonNull Optional<MessageRecord> nextMessageRecord,
                   @NonNull GlideRequests           glideRequests,
                   @NonNull Locale                  locale,
                   @NonNull Set<MessageRecord>      batchSelected,
                   @NonNull Recipient               conversationRecipient,
                            boolean                 pulseHighlight)
  {
    this.messageRecord = messageRecord;

    bubble.setBackgroundResource(BindableConversationItem.getCornerBackgroundRes(messageRecord, previousMessageRecord, nextMessageRecord, conversationRecipient.isGroupRecipient()));

    if (messageRecord.isOutgoing()) {
      bubble.getBackground().setColorFilter(ThemeUtil.getThemedColor(getContext(), R.attr.conversation_item_bubble_background), PorterDuff.Mode.MULTIPLY);
    } else {
      bubble.getBackground().setColorFilter(messageRecord.getRecipient().getColor().toConversationColor(getContext()), PorterDuff.Mode.MULTIPLY);
    }

    if (messageRecord.isOutgoingCall() || messageRecord.isIncomingCall()){
      callIconView.setColorFilter(getResources().getColor(R.color.core_green));
    } else if (messageRecord.isMissedCall()) {
      callIconView.setColorFilter(getResources().getColor(R.color.core_red));
    }

    callTextView.setText(messageRecord.getDisplayBody());
    dateView.setText(DateUtils.getExtendedRelativeTimeSpanString(getContext(), locale, messageRecord.getDateReceived()));
  }

  @Override
  public MessageRecord getMessageRecord() {
    return messageRecord;
  }

  @Override
  public void setEventListener(@Nullable EventListener listener) {

  }

  @Override
  public void unbind() {

  }
}
