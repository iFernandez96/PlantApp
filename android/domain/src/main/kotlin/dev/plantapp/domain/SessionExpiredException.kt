package dev.plantapp.domain

/** Thrown by the data layer when the API rejects the session (401 after any refresh
 *  attempt). The UI should route to sign-in. */
class SessionExpiredException : Exception("Session expired")
