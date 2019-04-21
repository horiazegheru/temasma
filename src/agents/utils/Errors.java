package agents.utils;

public enum Errors{
    NO_TILE_AT_COORDS("No tile at given coordinates"),
    NO_TILE_COLOR_AT_COORDS("No tile with given color at given coordinates"),
    OUT_OF_MAP("You are trying to step out of the map's bounds"),
    OBSTACLE("You are trying to step over an obstacle"),
    NO_HOLE_AT_COORDS("No hole at given coordinates"),
    NOT_CARRYING("You are not carrying any tiles but you are trying to drop"),
    NOT_FILLED_HOLE("You are stepping into a non-null depth hole");

    private String message;

    Errors(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}