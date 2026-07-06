package kr.jmsoup.hideandseek.network

enum class PacketAction(val id: String) {
    SHOW_NAME("show_name"),
    SET_MODE("set_mode"),
    UPDATE_SKIN("update_skin");

    companion object {
        fun fromId(id: String): PacketAction? = entries.find { it.id == id }
    }
}