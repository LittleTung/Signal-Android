package org.thoughtcrime.securesms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.components.CornerMaskingView;
import org.thoughtcrime.securesms.contactshare.Contact;
import org.thoughtcrime.securesms.contactshare.ContactUtil;
import org.thoughtcrime.securesms.database.RecipientDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.MmsMessageRecord;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientModifiedListener;
import org.thoughtcrime.securesms.util.ThemeUtil;
import org.thoughtcrime.securesms.util.Util;
import org.thoughtcrime.securesms.util.ViewUtil;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConversationSharedContactItem extends RelativeLayout
                                           implements BindableConversationItem, RecipientModifiedListener {

  private CornerMaskingView bubble;
  private ImageView         avatarView;
  private TextView          nameView;
  private TextView          numberView;
  private TextView          actionButtonView;

  private MessageRecord messageRecord;
  private EventListener eventListener;
  private Contact       contact;

  private final Map<String, Recipient> activeRecipients = new HashMap<>();

  public ConversationSharedContactItem(Context context) {
    super(context);
  }

  public ConversationSharedContactItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ConversationSharedContactItem(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bubble           = findViewById(R.id.contact_bubble);
    avatarView       = findViewById(R.id.contact_avatar);
    nameView         = findViewById(R.id.contact_name);
    numberView       = findViewById(R.id.contact_number);
    actionButtonView = findViewById(R.id.contact_action_button);
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
    this.contact       = ((MmsMessageRecord) messageRecord).getSharedContacts().get(0);

    resetActiveRecipients();
    presentBackground(messageRecord);
    presentContact(contact, locale);
    presentAvatar(contact.getAvatarAttachment() != null ? contact.getAvatarAttachment().getDataUri() : null, glideRequests);
    presentActionButtons(ContactUtil.getRecipients(getContext(), contact), contact);

    bubble.setRadii(BindableConversationItem.getCornerSpec(messageRecord, previousMessageRecord, nextMessageRecord, conversationRecipient.isGroupRecipient()));

    ViewUtil.setPaddingBottom(this, BindableConversationItem.getMessageSpacing(getContext(), messageRecord, nextMessageRecord));
  }

  @Override
  public MessageRecord getMessageRecord() {
    return messageRecord;
  }

  @Override
  public void setEventListener(@Nullable EventListener listener) {
    this.eventListener = listener;
  }

  @Override
  public void unbind() {
    this.eventListener = null;
    resetActiveRecipients();
  }

  @Override
  public void onModified(Recipient recipient) {
    Util.runOnMain(() -> presentActionButtons(Collections.singletonList(recipient), contact));
  }

  private void presentBackground(@NonNull MessageRecord message) {
    if (message.isOutgoing()) {
      bubble.setBackgroundColor(ThemeUtil.getThemedColor(getContext(), R.attr.conversation_item_bubble_background));
    } else {
      bubble.setBackgroundColor(message.getRecipient().getColor().toConversationColor(getContext()));
    }
  }

  private void presentContact(@Nullable Contact contact, @NonNull Locale locale) {
    if (contact != null) {
      nameView.setText(ContactUtil.getDisplayName(contact));
      numberView.setText(ContactUtil.getDisplayNumber(contact, locale));
    } else {
      nameView.setText("");
      numberView.setText("");
    }
  }

  private void presentAvatar(@Nullable Uri uri, @NonNull GlideRequests glideRequests) {
    if (uri != null) {
      glideRequests.load(new DecryptableUri(uri))
                   .fallback(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .dontAnimate()
                   .into(avatarView);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
    }
  }

  private void presentActionButtons(@NonNull List<Recipient> recipients, @NonNull Contact contact) {
    for (Recipient recipient : recipients) {
      activeRecipients.put(recipient.getAddress().serialize(), recipient);
    }

    List<Recipient> pushUsers   = new ArrayList<>(recipients.size());
    List<Recipient> systemUsers = new ArrayList<>(recipients.size());

    for (Recipient recipient : activeRecipients.values()) {
      recipient.addListener(this);

      if (recipient.getRegistered() == RecipientDatabase.RegisteredState.REGISTERED) {
        pushUsers.add(recipient);
      } else if (recipient.isSystemContact()) {
        systemUsers.add(recipient);
      }
    }

    if (!pushUsers.isEmpty()) {
      actionButtonView.setText(R.string.SharedContactView_message);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onMessageSharedContactClicked(pushUsers);
        }
      });
    } else if (!systemUsers.isEmpty()) {
      actionButtonView.setText(R.string.SharedContactView_invite_to_signal);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onInviteSharedContactClicked(systemUsers);
        }
      });
    } else {
      actionButtonView.setText(R.string.SharedContactView_add_to_contacts);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null && contact != null) {
          eventListener.onAddToContactsClicked(contact);
        }
      });
    }
  }

  private void resetActiveRecipients() {
    Stream.of(activeRecipients.values()).forEach(recipient ->  recipient.removeListener(this));
    this.activeRecipients.clear();
  }
}
