import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;

public class StylishLoginApp extends Application {
    private Connection con;
    private PreparedStatement ps;
    private Scene loginScene, mainScene;
    private Stage pstage;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage pstage) throws Exception {
        this.pstage = pstage;

        // Database connection setup
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/group", "root", "");

        // Create login elements
        Label loginLabel = new Label("Enter Client ID:");
        loginLabel.setFont(Font.font("Arial", 16));
        loginLabel.setStyle("-fx-text-fill: #FFA500;"); // Orange color

        TextField idtf = new TextField();
        idtf.setPromptText("Client ID");
        idtf.setStyle("-fx-background-color: #333333; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #AAAAAA;");
        idtf.setPrefWidth(300);
        idtf.setPrefHeight(40);

        Label passLabel = new Label("Enter Password:");
        passLabel.setFont(Font.font("Arial", 16));
        passLabel.setStyle("-fx-text-fill: #FFA500;"); // Orange color

        PasswordField passf = new PasswordField();
        passf.setPromptText("Password");
        passf.setStyle("-fx-background-color: #333333; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #AAAAAA;");
        passf.setPrefWidth(300);
        passf.setPrefHeight(40);

        Button loginb = new Button("Login");
        loginb.setStyle("-fx-background-color: #FFA500; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 16px;");
        loginb.setPrefWidth(300);
        loginb.setPrefHeight(50);

        // Button action
        loginb.setOnAction(e -> {
            int cid = Integer.parseInt(idtf.getText());
            try {
                ps = con.prepareStatement("SELECT pass FROM client WHERE cid = ?");
                ps.setInt(1, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (passf.getText().equals(rs.getString(1))) {
                        afterLogin(cid);
                    } else {
                        loginb.setText("WRONG PASS");
                    }
                } else {
                    loginLabel.setText("Incorrect ID, try again");
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: #FFA500; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 16px;");
        back.setPrefWidth(300);
        back.setPrefHeight(50);

        // Back button action
        back.setOnAction(e -> {
            // Navigate back to the main group application
            new maingroup().start(pstage);
        });

        VBox vbox = new VBox(15, loginLabel, idtf, passLabel, passf, loginb, back);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #000000; -fx-padding: 20;");

        loginScene = new Scene(vbox, 400, 350);
        loginScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        pstage.setTitle("Company Portal");
        pstage.setScene(loginScene);
        pstage.show();
    }
    private void afterLogin(int cid) {
        // Create buttons for navigation
        Button statsButton = new Button("Stats");
        statsButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 16px;");
        statsButton.setPrefHeight(50);
        statsButton.setOnAction(e -> afterLogin(cid));
    
        Button salesRepButton = new Button("Sales Rep");
        salesRepButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 16px;");
        salesRepButton.setPrefHeight(50);
        salesRepButton.setOnAction(e -> showSalesRepContent(cid));
    
        // Create a smaller back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12px;");
        backButton.setPrefSize(80, 30);  // Width and height adjusted to match font size
        backButton.setOnAction(e -> returnToLogin());
        backButton.getStyleClass().add("back-button");
    
        // Layout for customer ID and back button
        HBox topPanel = new HBox(10, backButton, new Label("Welcome Client ID: " + cid));
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setStyle("-fx-padding: 10; -fx-background-color: #000000;");
    
        // Fetch data for total sales of products
        VBox productSalesContent = new VBox();
        productSalesContent.setAlignment(Pos.CENTER);
        productSalesContent.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
    
        Label productSalesLabel = new Label("Product Sales");
        productSalesLabel.setStyle("-fx-text-fill: #FFA500;");
        productSalesLabel.setFont(Font.font("Arial", 24));
    
        ListView<String> productSalesList = new ListView<>();
        productSalesList.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
    
        // Execute query to fetch products and sales
        try {
            PreparedStatement psProductSales = con.prepareStatement("SELECT products, sales FROM company_s WHERE company_id = ?");
            psProductSales.setInt(1, cid);
            ResultSet rsProductSales = psProductSales.executeQuery();
            while (rsProductSales.next()) {
                String product = rsProductSales.getString("products");
                int sales = rsProductSales.getInt("sales");
                productSalesList.getItems().add(product + ": " + sales);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        productSalesContent.getChildren().addAll(productSalesLabel, productSalesList);
    
        // Fetch data for sales by sales representatives
        VBox salesRepContent = new VBox();
        salesRepContent.setAlignment(Pos.CENTER);
        salesRepContent.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
    
        Label salesRepLabel = new Label("Sales Rep Sales");
        salesRepLabel.setStyle("-fx-text-fill: #FFA500;");
        salesRepLabel.setFont(Font.font("Arial", 24));
    
        ListView<String> salesRepList = new ListView<>();
        salesRepList.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
        salesRepList.setCellFactory(listView -> new ListCell<>() {
            private final Text text = new Text();
    
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
    
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.setFill(Color.RED); // Change this to your desired color
                    setGraphic(text);
                }
            }
        });
        productSalesList.setCellFactory(listView -> new ListCell<>() {
            private final Text text = new Text();
    
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
    
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.setFill(Color.RED); // Change this to your desired color
                    setGraphic(text);
                }
            }
        });
    
        try {
            PreparedStatement psSalesRep = con.prepareStatement("SELECT sid, sales FROM sales WHERE company_id = ?");
            psSalesRep.setInt(1, cid);
            ResultSet rsSalesRep = psSalesRep.executeQuery();
            while (rsSalesRep.next()) {
                int sid = rsSalesRep.getInt("sid");
                int sales = rsSalesRep.getInt("sales");
    
                // Fetch sales rep name
                PreparedStatement psSalesRepName = con.prepareStatement("SELECT sname FROM sales_rep WHERE sid = ?");
                psSalesRepName.setInt(1, sid);
                ResultSet rsSalesRepName = psSalesRepName.executeQuery();
                if (rsSalesRepName.next()) {
                    String sname = rsSalesRepName.getString("sname");
                    salesRepList.getItems().add(sname + ": " + sales);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        salesRepContent.getChildren().addAll(salesRepLabel, salesRepList);
        
        // Layout for side-by-side display
        HBox centerPanel = new HBox(20, productSalesContent, salesRepContent);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setStyle("-fx-padding: 20;");
    
        // Adjust bottom panel buttons to touch the bottom and fill space
        HBox bottomPanel = new HBox();
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-background-color: #000000; -fx-padding: 0; -fx-spacing: 0;");
    
        HBox.setHgrow(statsButton, Priority.ALWAYS);
        HBox.setHgrow(salesRepButton, Priority.ALWAYS);
    
        bottomPanel.getChildren().addAll(statsButton, salesRepButton);
    
        // Adjust layout to ensure bottom panel touches bottom and fills space
        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(topPanel, centerPanel);
        VBox.setVgrow(centerPanel, Priority.ALWAYS);
    
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPanel);
        borderPane.setCenter(centerPanel);
        borderPane.setBottom(bottomPanel);
        BorderPane.setAlignment(borderPane.getBottom(), Pos.BOTTOM_CENTER);
        borderPane.setStyle("-fx-background-color: #000000; -fx-padding: 0;"); // Set padding to 0
    
        mainScene = new Scene(borderPane, 800, 550); // Adjusted scene size
        mainScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    
        pstage.setTitle("Company Portal - Dashboard");
        pstage.setScene(mainScene);
    }
    
    
    private void showSalesRepContent(int cid) {
        // Sales Rep view content
        VBox salesRepContent = new VBox(15);
        salesRepContent.setAlignment(Pos.TOP_CENTER);
        salesRepContent.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
        
        Label salesRepLabel = new Label("Sales Rep View");
        salesRepLabel.setStyle("-fx-text-fill: #FFA500;");
        salesRepLabel.setFont(Font.font("Arial", 24));
        
        ListView<String> salesRepList = new ListView<>();
        salesRepList.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
        salesRepList.setPrefWidth(250); // Decreased width
    
        // Fetch and display the sales representatives
        try {
            PreparedStatement psSalesReps = con.prepareStatement("SELECT sid, sname FROM sales_rep WHERE company_id = ?");
            psSalesReps.setInt(1, cid);
            ResultSet rsSalesReps = psSalesReps.executeQuery();
            while (rsSalesReps.next()) {
                int sid = rsSalesReps.getInt("sid");
                String sname = rsSalesReps.getString("sname");
                salesRepList.getItems().add(sid + ": " + sname);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        salesRepList.setCellFactory(listView -> new ListCell<>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.setFill(Color.RED); // Change this to your desired color
                    setGraphic(text);
                }
            }
        });
        // Add and Remove buttons
        HBox actionButtons = new HBox(20); // Increased spacing between buttons
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setStyle("-fx-padding: 20 0 50 0;"); // Added padding to create more gap between bottom panel
    
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 16px;");
        addButton.setOnAction(e -> {
            TextInputDialog addDialog = new TextInputDialog();
            addDialog.setHeaderText("Enter SID, Sname, Products, and Price separated by commas (e.g., 101, John Doe, ProductX, 100):");
            addDialog.setContentText("SID, Sname, Products, Price:");
    
            addDialog.showAndWait().ifPresent(input -> {
                String[] data = input.split(",");
                if (data.length == 4) {
                    try {
                        int sid = Integer.parseInt(data[0].trim());
                        String sname = data[1].trim();
                        String products = data[2].trim();
                        double price = Double.parseDouble(data[3].trim());
    
                        // Insert into sales_rep table
                        PreparedStatement psAddSalesRep = con.prepareStatement("INSERT INTO sales_rep (sid, sname, company_id,products,price) VALUES (?, ?, ?,?,?)");
                        psAddSalesRep.setInt(1, sid);
                        psAddSalesRep.setString(2, sname);
                        psAddSalesRep.setInt(3, cid);
                        psAddSalesRep.setString(4, products);
                        psAddSalesRep.setDouble(5, price);
                        psAddSalesRep.executeUpdate();
    
                        // Update the ListView
                        salesRepList.getItems().add(sid + ": " + sname + " - " + products + " @ $" + price);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
        

        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 16px;");
        removeButton.setOnAction(e -> {
            TextInputDialog removeDialog = new TextInputDialog();
            removeDialog.setHeaderText("Enter the SID of the Sales Rep to remove:");
            removeDialog.setContentText("SID:");
    
            removeDialog.showAndWait().ifPresent(sidStr -> {
                try {
                    int sid = Integer.parseInt(sidStr.trim());
                    PreparedStatement psRemove = con.prepareStatement("DELETE FROM sales_rep WHERE sid = ? AND company_id = ?");
                    psRemove.setInt(1, sid);
                    psRemove.setInt(2, cid);
                    int rowsAffected = psRemove.executeUpdate();
                    if (rowsAffected > 0) {
                        salesRepList.getItems().removeIf(item -> item.startsWith(sidStr.trim() + ":"));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });
    
        actionButtons.getChildren().addAll(addButton, removeButton);
    
        // Adding all components to the salesRepContent VBox
        salesRepContent.getChildren().addAll(salesRepLabel, salesRepList, actionButtons);
    
        // Set the center of the BorderPane to sales rep content
        BorderPane borderPane = (BorderPane) pstage.getScene().getRoot();
        borderPane.setCenter(salesRepContent);
    }
    
    private void returnToLogin() {
        try {
            new StylishLoginApp().start(pstage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}    