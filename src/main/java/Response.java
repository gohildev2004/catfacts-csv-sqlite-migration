/** What the router decided: a status code, a body to send as JSON, and a Location header or null. */
record Response(int status, Object body, String location) {}
