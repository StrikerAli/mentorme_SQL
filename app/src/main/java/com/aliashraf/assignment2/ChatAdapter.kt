package com.aliashraf.assignment2

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO
}

data class ChatMessage(
    var message: String = "",
    val sender: String = "",
    val timestamp: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val imageUrl: String = "",
    val videoUrl: String = ""
)
class ChatAdapter(private var messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TEXT_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message, parent, false)
                TextMessageViewHolder(view)
            }
            IMAGE_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_image, parent, false)
                ImageMessageViewHolder(view)
            }
            VIDEO_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_video, parent, false)
                VideoMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bindTextMessage(message)
            is ImageMessageViewHolder -> holder.bindImageMessage(message)
            is VideoMessageViewHolder -> holder.bindVideoMessage(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.messageType) {
            MessageType.TEXT -> TEXT_MESSAGE_VIEW_TYPE
            MessageType.IMAGE -> IMAGE_MESSAGE_VIEW_TYPE
            MessageType.VIDEO -> VIDEO_MESSAGE_VIEW_TYPE
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.textTime)
        private val requestQueue: RequestQueue = Volley.newRequestQueue(itemView.context)

        fun bindTextMessage(chatMessage: ChatMessage) {
            messageText.text = chatMessage.message
            timestampText.text = chatMessage.timestamp

            // Set click listener for deleting message
            itemView.setOnClickListener {
                deleteMessage(chatMessage)
            }

            // Set long click listener for editing message
            itemView.setOnLongClickListener {
                showEditMessageDialog(itemView.context, chatMessage)
                true // Indicate that the long click event is consumed
            }
        }

        private fun deleteMessage(chatMessage: ChatMessage) {
            val url = "http://192.168.18.32/update_message.php?action=delete&message=${chatMessage.message}"
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    Log.d("ChatAdapter", "Message deleted successfully")
                    Toast.makeText(itemView.context, "Message deleted", Toast.LENGTH_SHORT).show()
                    val remainingMessages = messages.filterNot { it == chatMessage }
                    updateMessages(remainingMessages)
                },
                { error ->
                    // Handle error
                    Log.d("ChatAdapter", "Error deleting message: $error")
                    Toast.makeText(itemView.context, error.message, Toast.LENGTH_SHORT).show()
                    // For example, you can show an error message to the user
                }
            )
            requestQueue.add(jsonObjectRequest)
        }

        private fun showEditMessageDialog(context: Context, chatMessage: ChatMessage) {
            val editText = EditText(context)
            editText.setText(chatMessage.message)

            AlertDialog.Builder(context)
                .setTitle("Edit Message")
                .setMessage("Edit your message:")
                .setView(editText)
                .setPositiveButton("Save") { dialog, _ ->
                    val editedMessage = editText.text.toString()
                    editMessage(chatMessage, editedMessage)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun editMessage(chatMessage: ChatMessage, newMessage: String) {
            val url = "http://192.168.18.32/update_message.php?action=edit&message=${chatMessage.message}&newMessage=$newMessage"
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    // Handle successful response
                Log.d("JSON", "Message edited successfully")
                    Toast.makeText(itemView.context, "Message edited", Toast.LENGTH_SHORT).show()
                    // For example, you can update the UI to reflect the edited message
                    val editedMessages = messages.map {
                        if (it == chatMessage) {
                            it.copy(message = newMessage) // Update the message content
                        } else {
                            it // Keep other messages unchanged
                        }
                    }
                },
                { error ->
                    // Handle error
                    Log.d("JSON", "Error editing message: $error")
                    Toast.makeText(itemView.context, error.message, Toast.LENGTH_SHORT).show()
                    // For example, you can show an error message to the user
                }
            )
            requestQueue.add(jsonObjectRequest)
        }
    }

    inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.textTime)

        fun bindImageMessage(chatMessage: ChatMessage) {
            val imageurl = "http://192.168.18.32/uploads/"+chatMessage.message
            Log.d("JSON", "Image URL: ${imageurl}")
            Picasso.get().load(imageurl).into(imageView)
            timestampText.text = chatMessage.timestamp
        }
    }

    inner class VideoMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoView: VideoView = itemView.findViewById(R.id.VideoMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.textTime)

        fun bindVideoMessage(chatMessage: ChatMessage) {
            val videourl = "http://192.168.18.32/uploads/"+chatMessage.message
            Log.d("JSON", "Video URL: ${videourl}")
            videoView.setVideoURI(Uri.parse(videourl))
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.setLooping(true)
                mediaPlayer.start()
                mediaPlayer.setVolume(0f, 0f)

            }
            timestampText.text = chatMessage.timestamp
        }
    }

    companion object {
        private const val TEXT_MESSAGE_VIEW_TYPE = 1
        private const val IMAGE_MESSAGE_VIEW_TYPE = 2
        private const val VIDEO_MESSAGE_VIEW_TYPE = 3
    }
}







