module de.vladistic.sokoban {
    requires javafx.controls;
    requires javafx.fxml;


    opens de.vladistic.sokoban to javafx.fxml;
    exports de.vladistic.sokoban;
}