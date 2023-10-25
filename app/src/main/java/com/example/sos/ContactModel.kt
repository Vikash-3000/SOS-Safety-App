package com.example.sos

class ContactModel (id : Int, phone : String, name : String) {
    var id : Int = id
        get() = field

    val phone : String = validate(phone)
        get() = field

    var name : String = name
        get() = field
        set(value) {
            field = value
        }

    private fun validate(phone: String) : String {
        val case1 = StringBuilder("+91")
        val case2 = StringBuilder()

        return if (phone[0] != '+') {
            for (i in phone.indices) {
                // remove any spaces or "-"
                if (phone[i] != '-' && phone[i] != ' ') {
                    case1.append(phone[i])
                }
            }
            case1.toString()
        } else {
            for (i in phone.indices) {
                // remove any spaces or "-"
                if (phone[i] != '-' || phone[i] != ' ') {
                    case2.append(phone[i])
                }
            }
            case2.toString()
        }
    }
}