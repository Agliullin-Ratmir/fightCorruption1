package entities;


public enum Steps {
    GET_ACTUAL_PRICES("GET_ACTUAL_PRICES"),
    CREATE_FILE("CREATE_FILE"),
    SEND_MAIL("SEND_MAIL");

    private final String step;

    Steps(String s) {
        step = s;
    }

    public boolean equals(String otherName) {
        if (otherName == null) {
            return false;
        }
        return step.equals(otherName);
    }

    @Override
    public String toString() {
        return step;
    }
}
