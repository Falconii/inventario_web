package com.simionato.inventarioweb.shared

class HttpErrorMessage {
    private var code = 0
    private var message: String? = null

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }
}