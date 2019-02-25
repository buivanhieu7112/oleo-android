package com.framgia.oleo.screen.boxchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framgia.oleo.R
import com.framgia.oleo.data.source.model.Message
import com.framgia.oleo.data.source.model.User
import com.framgia.oleo.utils.Index
import com.framgia.oleo.utils.extension.hide
import kotlinx.android.synthetic.main.adapter_message_receive.view.*
import kotlinx.android.synthetic.main.adapter_message_send.view.*

class BoxChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = arrayListOf<Message>()
    private lateinit var user: User
    private var imageUrl: String? = null

    fun updateData(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.lastIndex)
    }

    fun updateOldData(message: ArrayList<Message>) {
        if (messages[Index.POSITION_ZERO].id != message[Index.POSITION_ZERO].id) {
            message.removeAt(message.lastIndex)
            messages.addAll(Index.POSITION_ZERO, message)
            notifyItemRangeInserted(Index.POSITION_ZERO, message.size)
        }
    }

    fun setUser(user: User) {
        this.user = user
    }

    fun setUserFriendImage(url: String) {
        imageUrl = url
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SEND) MessageSendHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.adapter_message_send, viewGroup, false
            )
        ) else MessageReceiveHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.adapter_message_receive, viewGroup, false
            )
        )
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        when (holder) {
            is MessageSendHolder -> {
                if (pos == Index.POSITION_ZERO) {
                    holder.bindData(messages[pos], user.image)
                    return
                }
                if (messages[pos - Index.POSITION_ONE].userId == messages[pos].userId) {
                    holder.bindDataNoImage(messages[pos])
                    return
                }
                holder.bindData(messages[pos], user.image)
            }
            is MessageReceiveHolder -> {
                if (pos == Index.POSITION_ZERO) {
                    holder.bindData(messages[pos], imageUrl)
                    return
                }
                if (messages[pos - Index.POSITION_ONE].userId == messages[pos].userId) {
                    holder.bindDataNoImage(messages[pos])
                    return
                }
                holder.bindData(messages[pos], imageUrl)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].userId!! == user.id) VIEW_TYPE_MESSAGE_SEND
        else VIEW_TYPE_MESSAGE_RECEIVE
    }

    class MessageSendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(message: Message, image: String?) {
            itemView.textSendMessage.text = message.message
            Glide.with(itemView.context).load(image)
                .apply(RequestOptions().circleCrop().placeholder(R.drawable.ic_user_circle))
                .into(itemView.imageSendMessage)
            itemView.textSendTime.text = message.time
        }

        fun bindDataNoImage(message: Message) {
            itemView.textSendMessage.text = message.message
            itemView.imageSendMessage.hide()
            itemView.textSendTime.text = message.time
        }
    }

    class MessageReceiveHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(message: Message, imageUrl: String?) {
            itemView.textReceiveMessage.text = message.message
            Glide.with(itemView.context).load(imageUrl)
                .apply(RequestOptions().circleCrop().placeholder(R.drawable.ic_user_circle))
                .into(itemView.imageReceiveMessage)
            itemView.textReceiveTime.text = message.time
        }

        fun bindDataNoImage(message: Message) {
            itemView.textReceiveMessage.text = message.message
            itemView.imageReceiveMessage.hide()
            itemView.textReceiveTime.text = message.time
        }
    }

    companion object {
        const val VIEW_TYPE_MESSAGE_SEND: Int = 0
        const val VIEW_TYPE_MESSAGE_RECEIVE: Int = 1
    }
}
