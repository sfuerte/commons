/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.resource;

// Java SE
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.forgerock.json.fluent.JsonValue;

/**
 * An exception that is thrown during the processing of a JSON resource request.
 * Contains an integer exception code and short reason phrase. A longer
 * description of the exception is provided in the exception's detail message.
 * <p>
 * Positive 3-digit integer exception codes are explicitly reserved for
 * exceptions that correspond with HTTP status codes. For the sake of
 * interoperability with HTTP, if an exception corresponds with an HTTP error
 * status, use the matching HTTP status code.
 */
public class ResourceException extends ExecutionException {

    /**
     * Indicates that the request could not be understood by the resource due to
     * malformed syntax. Equivalent to HTTP status: 400 Bad Request.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Indicates the request could not be completed due to a conflict with the
     * current state of the resource. Equivalent to HTTP status: 409 Conflict.
     */
    public static final int CONFLICT = 409;

    /**
     * Indicates that the resource understood the request, but is refusing to
     * fulfill it. Equivalent to HTTP status: 403 Forbidden.
     */
    public static final int FORBIDDEN = 403;

    /**
     * Indicates that a resource encountered an unexpected condition which
     * prevented it from fulfilling the request. Equivalent to HTTP status: 500
     * Internal Server Error.
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     * Indicates that the resource could not be found. Equivalent to HTTP
     * status: 404 Not Found.
     */
    public static final int NOT_FOUND = 404;

    /**
     * Indicates that the resource does not implement/support the feature to
     * fulfill the request HTTP status: 501 Not Implemented.
     */
    public static final int NOT_SUPPORTED = 501;

    /**
     * Indicates that the resource is temporarily unable to handle the request.
     * Equivalent to HTTP status: 503 Service Unavailable.
     */
    public static final int UNAVAILABLE = 503;

    /**
     * Indicates that the resource's current version does not match the version
     * provided. Equivalent to HTTP status: 412 Precondition Failed.
     */
    public static final int VERSION_MISMATCH = 412;

    /**
     * Indicates that the resource requires a version, but no version was
     * supplied in the request. Equivalent to
     * draft-nottingham-http-new-status-03 HTTP status: 428 Precondition
     * Required.
     */
    public static final int VERSION_REQUIRED = 428;

    /** Serializable class a version number. */
    private static final long serialVersionUID = 1L;

    /**
     * Returns an exception with the specified exception code, reason phrase,
     * detail message and cause. Useful for translating HTTP status codes to the
     * relevant Java exception type. The type of the returned exception will be
     * a sub-type of ResourceException.
     *
     * @param code
     *            The numeric code of the exception.
     * @param reason
     *            The short reason phrase of the exception, or null if this
     *            implementation should assign a short reason phrase.
     * @param message
     *            The detail message.
     * @param cause
     *            The exception which caused this exception to be thrown.
     * @return A resource exception having the provided code, reason, message,
     *         and cause.
     */
    public static ResourceException getException(final int code, final String reason,
            final String message, final Throwable cause) {
        ResourceException ex = null;
        switch (code) {
        case BAD_REQUEST:
            ex = new BadRequestException(message, cause);
            break;
        case FORBIDDEN:
            ex = new ForbiddenException(message, cause);
            break; // Authorization exceptions
        case NOT_FOUND:
            ex = new NotFoundException(message, cause);
            break;
        case CONFLICT:
            ex = new ConflictException(message, cause);
            break;
        case VERSION_MISMATCH:
            ex = new PreconditionFailedException(message, cause);
            break;
        case VERSION_REQUIRED:
            ex = new PreconditionRequiredException(message, cause);
            break; // draft-nottingham-http-new-status-03
        case INTERNAL_ERROR:
            ex = new InternalServerErrorException(message, cause);
            break;
        case NOT_SUPPORTED:
            ex = new NotSupportedException(message, cause);
            break; // Not Implemented
        case UNAVAILABLE:
            ex = new ServiceUnavailableException(message, cause);
            break;

        // Temporary failures without specific exception classes
        case 408: // Request Time-out
        case 504: // Gateway Time-out
            ex = new RetryableException(code, message, cause);
            break;

        // Permanent Failures without specific exception classes
        case 401: // Unauthorized - Missing or bad authentication
        case 402: // Payment Required
        case 405: // Method Not Allowed
        case 406: // Not Acceptable
        case 407: // Proxy Authentication Required
        case 410: // Gone
        case 411: // Length Required
        case 413: // Request Entity Too Large
        case 414: // Request-URI Too Large
        case 415: // Unsupported Media Type
        case 416: // Requested range not satisfiable
        case 417: // Expectation Failed
        case 502: // Bad Gateway
        case 505: // HTTP Version not supported
            ex = new PermanentException(code, message, cause);
            break;
        default:
            ex = new UncategorizedException(code, message, cause);
        }
        if (reason != null) {
            ex.setReason(reason);
        }

        return ex;
    }

    /**
     * Returns the reason phrase for an HTTP error status code, per RFC 2616 and
     * draft-nottingham-http-new-status-03. If no match is found, then a generic
     * reason {@code "Resource Exception"} is returned.
     */
    private static String reason(final int code) {
        String result = "Resource Exception"; // default
        switch (code) {
        case BAD_REQUEST:
            result = "Bad Request";
            break;
        case 401:
            result = "Unauthorized";
            break; // Missing or bad authentication (despite the name)
        case 402:
            result = "Payment Required";
            break;
        case FORBIDDEN:
            result = "Forbidden";
            break; // Authorization exceptions
        case NOT_FOUND:
            result = "Not Found";
            break;
        case 405:
            result = "Method Not Allowed";
            break;
        case 406:
            result = "Not Acceptable";
            break;
        case 407:
            result = "Proxy Authentication Required";
            break;
        case 408:
            result = "Request Time-out";
            break;
        case CONFLICT:
            result = "Conflict";
            break;
        case 410:
            result = "Gone";
            break;
        case 411:
            result = "Length Required";
            break;
        case VERSION_MISMATCH:
            result = "Precondition Failed";
            break;
        case 413:
            result = "Request Entity Too Large";
            break;
        case 414:
            result = "Request-URI Too Large";
            break;
        case 415:
            result = "Unsupported Media Type";
            break;
        case 416:
            result = "Requested range not satisfiable";
            break;
        case 417:
            result = "Expectation Failed";
            break;
        case VERSION_REQUIRED:
            result = "Precondition Required";
            break; // draft-nottingham-http-new-status-03
        case INTERNAL_ERROR:
            result = "Internal Server Error";
            break;
        case NOT_SUPPORTED:
            result = "Not Implemented";
            break;
        case 502:
            result = "Bad Gateway";
            break;
        case UNAVAILABLE:
            result = "Service Unavailable";
            break;
        case 504:
            result = "Gateway Time-out";
            break;
        case 505:
            result = "HTTP Version not supported";
            break;
        }
        return result;
    }

    /** The numeric code of the exception. */
    private final int code;

    /** The short reason phrase of the exception. */
    private String reason;

    /** Additional detail which can be evaluated by applications. */
    private final JsonValue detail = new JsonValue(null);

    /**
     * Constructs a new exception with the specified exception code, and
     * {@code null} as its detail message. If the error code corresponds with a
     * known HTTP error status code, then the reason phrase is set to a
     * corresponding reason phrase, otherwise is set to a generic value
     * {@code "Resource Exception"}.
     *
     * @param code
     *            The numeric code of the exception.
     */
    protected ResourceException(final int code) {
        this.code = code;
        this.reason = reason(code);
    }

    /**
     * Constructs a new exception with the specified exception code and detail
     * message.
     *
     * @param code
     *            The numeric code of the exception.
     * @param message
     *            The detail message.
     */
    protected ResourceException(final int code, final String message) {
        super(message);
        this.code = code;
        this.reason = reason(code);
    }

    /**
     * Constructs a new exception with the specified exception code, reason
     * phrase, detail message and cause.
     *
     * @param code
     *            The numeric code of the exception.
     * @param reason
     *            The short reason phrase of the exception, or null to have this
     *            implementation assign a reason String
     * @param message
     *            The detail message.
     * @param cause
     *            The exception which caused this exception to be thrown.
     */
    protected ResourceException(final int code, final String reason, final String message,
            final Throwable cause) {
        super(message, cause);
        this.code = code;
        if (reason != null) {
            this.reason = reason;
        } else {
            this.reason = reason(code);
        }
    }

    /**
     * Constructs a new exception with the specified exception code, reason
     * phrase and cause. The detail message is initialized to the detail message
     * from the specified cause.
     *
     * @param code
     *            The numeric code of the exception.
     * @param message
     *            The detail message.
     * @param cause
     *            The exception which caused this exception to be thrown.
     */
    protected ResourceException(final int code, final String message, final Throwable cause) {
        this(code, reason(code), message, cause);
    }

    /**
     * Constructs a new exception with the specified exception code and cause.
     *
     * @param code
     *            The numeric code of the exception.
     * @param cause
     *            The exception which caused this exception to be thrown.
     */
    protected ResourceException(final int code, final Throwable cause) {
        super(cause);
        this.code = code;
        this.reason = reason(code);
    }

    /**
     * Returns the numeric code of the exception.
     *
     * @return The numeric code of the exception.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the additional detail which can be evaluated by applications. By
     * default there is no additional detail (
     * {@code getDetail().isNull() == true}), and it is the responsibility of
     * the resource provider to add it if needed.
     *
     * @return The additional detail which can be evaluated by applications
     *         (never {@code null}).
     */
    public JsonValue getDetail() {
        return detail;
    }

    /**
     * Returns the short reason phrase of the exception.
     *
     * @return The short reason phrase of the exception.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets/overrides the short reason phrase of the exception.
     *
     * @param reason
     *            The short reason phrase of the exception.
     */
    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * Returns the exception in a JSON object structure, suitable for inclusion
     * in the entity of an HTTP error response.
     *
     * @return The exception in a JSON object structure, suitable for inclusion
     *         in the entity of an HTTP error response.
     */
    public JsonValue toJsonValue() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>(4);
        result.put("error", code); // required
        if (reason != null) { // optional
            result.put("reason", reason);
        }
        final String message = getMessage();
        if (message != null) { // optional
            result.put("message", message);
        }
        result.put("detail", detail.getObject());
        return new JsonValue(result);
    }
}
