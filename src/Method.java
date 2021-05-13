public enum Method {
    REGISTER,
    LOGIN,
    LOGOUT,
    INFORM,
    LIST, // all file names
    DETAILS,
    CHECK_ACTIVE_PEER_TO_PEER,
    CHECK_ACTIVE_TRACKER_TO_PEER,
    SIMPLE_DOWNLOAD,
    NOTIFY_SUCCESSFUL,
    NOTIFY_FAILED,
    SEEDER_SERVE,
    COLLABORATIVE_DOWNLOAD,
    SEEDER_SERVE_SUCCESSFUL,
    COLLABORATIVE_DOWNLOAD_NOT_ANSWER // this will be sent by a peer to another peer in order to inform him that he doesnt need a partition (he has them all)

}
