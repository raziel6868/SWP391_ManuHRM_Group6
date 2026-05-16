package model;

import java.sql.Timestamp;

public class PasswordReset {


    public enum Status {
        PENDING, RESOLVED, REJECTED
    }

    private Long id;
    private Long userId;
    private Status status;
    private Timestamp createdAt;
    private Long resolvedBy;
}
