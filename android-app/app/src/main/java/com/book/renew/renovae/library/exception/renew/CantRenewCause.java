package com.book.renew.renovae.library.exception.renew;

import com.book.renew.renovae.util.Util;

import java.io.Serializable;

/**
 * Class to represent why the user can't renew a borrow
 */
public class CantRenewCause implements Serializable {
    /**
     * Enum indicating why you can't renew
     */
    public enum Cause implements Serializable {
        OVERDUE("Você tem empréstimo(s) em atraso"),
        MAX_RENEWS("Máximo de renovações alcançado"),
        CANT_EXTEND("O prazo de devolução não pode ser estendido"),
        UNKNOWN("Não pode ser renovado");

        Cause(String default_message) {
            this._message = default_message;
        }
        private String _message;
    }

    private Cause _cause;
    private String _msg;

    /**
     * Creates a cause with default message
     * @param cause
     */
    public CantRenewCause(Cause cause) {
        this(cause, null);
    }

    /**
     * Creates a cause with message (or default message if message
     * is null or empty)
     * @param cause
     * @param message
     */
    public CantRenewCause(Cause cause, String message) {
        this._cause = cause;
        this._msg = (Util.isEmpty(message) ? cause._message : message);
    }

    /**
     * Gets the cause you can't renew
     * @return Cause
     */
    public Cause getEnumCause() {
        return _cause;
    }

    /**
     * Gets the error message
     * @return
     */
    public String getMessage() {
        return _msg;
    }
}
