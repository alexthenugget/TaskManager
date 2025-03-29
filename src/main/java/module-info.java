module com.github.alex_the_nugget.taskhub.taskhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.github.alex_the_nugget.taskhub.taskhub.controllers to javafx.fxml;
    opens com.github.alex_the_nugget.taskhub.taskhub to javafx.fxml;
    exports com.github.alex_the_nugget.taskhub.taskhub.controllers to javafx.fxml;
    exports com.github.alex_the_nugget.taskhub.taskhub;
    requires telegrambots;
    requires telegrambots.meta;
    requires org.postgresql.jdbc;
    requires org.slf4j;
    requires java.desktop;
}