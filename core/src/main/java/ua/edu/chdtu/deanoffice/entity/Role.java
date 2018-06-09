package ua.edu.chdtu.deanoffice.entity;

public enum Role {
    ADMIN, DEAN_OFFICER, ACTUATOR;

    public String authority() {
        return "ROLE_" + this.name();
    }
}
