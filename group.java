// hello im rudra
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Optional;

import javax.naming.spi.DirStateFactory.Result;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class group extends Application {
   Connection con;
    PreparedStatement ps;
    Statement stmt;
    Scene loginScene;
    Stage pstage;
    static BST bst;
    public static void main(String[] args)throws Exception {
       
        launch(args); 
    }

    @Override
    public void start(Stage pstage) throws Exception {
        this.pstage=pstage;
        bst=new BST();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/group", "root", "");
        Statement st=conn.createStatement();
        ResultSet rst=st.executeQuery("SELECT sname,products,price,sid FROM sales_rep;");
        while(rst.next())
        {
            bst.insertByPro(rst.getString(1), rst.getString(2), rst.getInt(3),rst.getInt(4));
        }
      // bst.insertByPro("buda","mobile",20000,2);
        bst.printInOrder();
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/group", "root", "");
        
       Label loginLabel=new Label("Enter Client Id:");
        TextField idtf=new TextField();
        Label passLabel=new Label("Enter Password:");
        PasswordField passf=new PasswordField();
        Button loginb=new Button("Login");
        loginb.setOnAction(e ->
        {
            int cid=Integer.parseInt(idtf.getText());
            try {
                ps=con.prepareStatement("select pass from client where cid="+cid);
                ResultSet rs=ps.executeQuery();
                if(rs.next())
                {
                    if(passf.getText().equals(rs.getString(1)))
                    {
                        afterlogin(cid);
                    }
                    else
                    {
                        loginb.setText("WRONG PASS");
                    }
                }
                else
                {
                    loginLabel.setText("Incorrect ID try again");
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            
        });

        Label blankLabel=new Label();
        Button back=new Button("Back");
       
        back.setOnAction(e ->{
            new maingroup().start(pstage);
        });

        VBox v=new VBox(10,loginLabel,idtf,passLabel,passf,loginb,back);
        v.setAlignment(Pos.CENTER);
        v.setStyle("-fx-padding: 20;");
        loginScene=new Scene(v,350,300);
        loginScene.getStylesheets().add(getClass().getResource("stylegroup.css").toExternalForm());
         back.getStyleClass().add("back-button");
        pstage.setTitle("LOGIN");
        pstage.setScene(loginScene);
        pstage.show();
    }
 Scene homeScene;
    void afterlogin(int cid) throws Exception
    {
        
        Label currentdemandLabel=new Label("Current Demands:");
        TextArea demandta=new TextArea();
        Button editb=new Button("Edit");
        editb.getStyleClass().add("edit-button");
        editb.setOnAction(e -> {
            try {
                editdemand(cid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        ps=con.prepareStatement("select demand,demand_count from client where cid="+cid);
        ResultSet rs=ps.executeQuery();
        int i1=0;
        while(rs.next())
        {
            String demand=rs.getString(1);
            int demand_count=rs.getInt(2);
            demandta.appendText(++i1+") "+demand+" "+demand_count+"\n");
        }
        demandta.setEditable(false);
        Label dealsta=new Label("Current Deals:");
        ListView<ll1.node> dealsListView = new ListView<>();
        ObservableList<ll1.node> dealsList = FXCollections.observableArrayList();

        
            String sql1 = "SELECT deal.product, deal.demand_count, sales_rep.sname,deal.sid FROM DEAL INNER JOIN sales_rep ON deal.sid = sales_rep.sid WHERE lead<>'loss';";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            ll1 l=new ll1();
            while (rs1.next()) {
                
                dealsList.add(l.insert(rs1.getInt(4), rs1.getString(3), rs1.getString(1)));
            
            }
            dealsListView.setItems(dealsList);
            
      

        // Add click event to list items
        dealsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                if(!dealsListView.getItems().isEmpty())
                {
                    
                   //System.out.println(dealsListView.getSelectionModel().getSelectedItem().sid);
                    try {
                        String selectedDeal = dealsListView.getSelectionModel().getSelectedItem().product;
                        if (selectedDeal != null) {
                        nextlead(cid,dealsListView.getSelectionModel().getSelectedItem().sid, dealsListView.getSelectionModel().getSelectedItem().product);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
                else
                {
                    showAlert("It is Empty!!", "Empty", AlertType.WARNING);
                }
               
            }
        });
          // UI components
        TextField searchField = new TextField();
        searchField.setPromptText("Search Here...");
        Label searchlabel=new Label("Search:");
        // Create Button to trigger the search
       

        // Create ListView to display search results
        ListView<BST.Node> resultsListView = new ListView<>();
        resultsListView.setItems(bst.search(""));

        // Add action to the button to perform the search
        // searchButton.setOnAction(event -> {
        //     String query = searchField.getText();
        //     ObservableList<BST.Node> results = bst.search(query);
        //     resultsListView.setItems(results);
        // });
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<BST.Node> results = bst.search(newValue);
            resultsListView.setItems(results);
        });
        // Handle item clicks to open a new page
        resultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // double-click
                BST.Node selectedItem = resultsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                   try {
                    
                    makedeal(cid,selectedItem.getSid(),selectedItem.getProduct());
                    } catch (Exception e) {
                    e.printStackTrace();
                    }
                }
                
            }
        });
        Label reportLabel=new Label("Generate Report: ");
        Button reportb=new Button("Download");

        reportb.setOnAction(e ->
        {
            
            try {
                PreparedStatement getcname=con.prepareStatement("SELECT cname,LOCALTIMESTAMP FROM client WHERE cid="+cid);
                ResultSet getcnamers=getcname.executeQuery();
                getcnamers.next();
                String cname1=getcnamers.getString(1);
                String cname=""+cname1+getcnamers.getString(2).replace(":", "-").replace(".", "-");
                File f=new File("C:\\Users\\rmp20\\Downloads\\"+ cname +".txt");
                f.createNewFile();
                BufferedWriter bw=new BufferedWriter(new FileWriter(f));
                bw.write("NAME: "+cname1);
                bw.newLine();
                bw.write("Customer ID: "+cid+"\n \n");
                bw.write("~~ Your Demands ~~\n");
                ps=con.prepareStatement("SELECT demand,demand_count FROM client WHERE cid="+cid);
                ResultSet rs2=ps.executeQuery();
                while(rs2.next())
                {
                    bw.write(rs2.getString(1)+" "+rs2.getString(2)+"\n");
                }
                bw.newLine();
                bw.write("~~ Total Deals ~~\n");
                stmt=con.createStatement();
                ResultSet rs3=stmt.executeQuery("SELECT sid,product,lead FROM deal WHERE cid="+cid);
                int i=0;
                while (rs3.next()) {
                    int sid1=rs3.getInt(1);
                    String product1=rs3.getString(2);
                    String lead1=rs3.getString(3);
                     ps=con.prepareStatement("SELECT sname,price FROM sales_rep WHERE sid=? AND products=?");
                     ps.setInt(1, sid1);
                     ps.setString(2, product1);
                     ResultSet rs4=ps.executeQuery();
                     rs4.next();
                     String sname1=rs4.getString(1);
                     int price=rs4.getInt(2);
                    bw.write((++i)+") Sales_Rep. = "+sname1+" Product = "+product1+" Price = "+price+" Stage = "+lead1);
                    bw.newLine();
                }
                i=0;

                bw.flush();
                showAlert("Download Successfull", "Download!!", AlertType.INFORMATION);

            } catch (Exception e1) {
               
                e1.printStackTrace();
            }
        });

        Button back=new Button("Back");
        back.setOnAction(e ->
        {
            pstage.setTitle("LOGIN");
            pstage.setScene(loginScene);
        });
        GridPane grid=new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));
                    //column row
        grid.add(currentdemandLabel, 0, 0);
        grid.add(demandta, 1, 0);
        grid.add(editb,2,0);
        grid.add(dealsta, 0, 1);
        grid.add(dealsListView, 1, 1);
        HBox h=new HBox(10,reportLabel,reportb);
        grid.add(h,1,2);
        grid.add(searchlabel,0,3);
        grid.add(searchField, 1, 3);        
        grid.add(resultsListView, 1, 4);
        grid.add(back, 0, 6);
         
        
         ColumnConstraints col1 = new ColumnConstraints();
         col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(10);
        ColumnConstraints col3 = new ColumnConstraints();
        col2.setPercentWidth(60);
         grid.getColumnConstraints().addAll(col1, col2,col3);
        

        
        homeScene= new Scene(grid, 450, 400);
        homeScene.getStylesheets().add(getClass().getResource("stylegroup.css").toExternalForm());

        pstage.setTitle("HOME PAGE");
        pstage.setScene(homeScene);
    }
Scene editScene;
    void editdemand(int cid) throws Exception
    {
        stmt=con.createStatement();
        TextArea demandArea=new TextArea();
        int i=0;
        ResultSet rs=stmt.executeQuery("SELECT demand,demand_count FROM client where cid="+cid);
        while(rs.next())
        {
            demandArea.appendText(++i+") "+rs.getString(1)+" "+rs.getString(2)+"\n");
        }
        demandArea.setDisable(true);
        
        Button add=new Button("Add");
        Button remove=new Button("Remove");
        
        Dialog <Pair<String,Integer>> dl=new Dialog<>();
        dl.setTitle("Enter Details");
        dl.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField dtf=new TextField();
        TextField dctf=new TextField();
        VBox v=new VBox(10,new Label("Enter Demand:"),dtf,new Label("Enter Count"),dctf);
        dl.getDialogPane().setContent(v);
        dl.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(dtf.getText(), Integer.parseInt(dctf.getText()));
            }
            return null;
        });

        add.setOnAction(e -> {
           Optional<Pair<String, Integer>> op=dl.showAndWait();
           if(op.isPresent())
           {
            Pair<String, Integer> pr=op.get();
            String demand1=pr.left;
            int dcount=pr.right;
            try {
                ps=con.prepareStatement("INSERT INTO client values(?,?,?,?,?)");
                ps.setInt(1, cid);
               ResultSet rs1=stmt.executeQuery("SELECT cname FROM client_detail WHERE cid="+cid);
                rs1.next();
                String cname=rs1.getString(1);
                ps.setString(2,cname);
                ps.setString(3, demand1);
                ps.setInt(4, dcount);
                ps.setInt(5, 0);
                ps.executeUpdate();
                editdemand(cid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

           }
        });

        remove.setOnAction(e -> {
            Optional<Pair<String, Integer>> op=dl.showAndWait();
            if(op.isPresent())
            {
             Pair<String, Integer> pr=op.get();
             String demand1=pr.left;
             int dcount=pr.right;
             try {
                 ps=con.prepareStatement("DELETE FROM client WHERE cid=? AND demand=? AND demand_count=?");
                 ps.setInt(1, cid);
                 ps.setString(2, demand1);
                 ps.setInt(3, dcount);
                 ps.executeUpdate();
                 editdemand(cid);
             } catch (Exception e1) {
                 e1.printStackTrace();
             }
 
            }
            
 
         });


        Button back=new Button("Back");
        back.setOnAction(e ->{
            try {
                afterlogin(cid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        GridPane gp=new GridPane();
       gp.add(demandArea, 0, 0);
       gp.add(add, 0, 2);
       gp.add(remove, 1, 2);
       gp.add(back, 0, 4);
       gp.add(new Label(),0,1);
       gp.add(new Label(),0,3);

       ColumnConstraints col1 = new ColumnConstraints();
       col1.setPercentWidth(50);
       ColumnConstraints col2 = new ColumnConstraints();
       col2.setPercentWidth(50);
       gp.getColumnConstraints().addAll(col1, col2);

        editScene=new Scene(gp,350,200);
        editScene.getStylesheets().add("stylegroup.css");
        pstage.setScene(editScene);
        pstage.setTitle("Edit Demands");
        
    }

Scene makedealScene;
void makedeal(int cid,int sid,String product) throws Exception{
    Label productLabel = new Label("Product : "+ product);
    PreparedStatement pn = con.prepareStatement("select sname,price from sales_rep where sid = "+sid);
    ResultSet rs = pn.executeQuery();
    ps=con.prepareStatement("SELECT company_name FROM company_s WHERE company_id IN(SELECT company_id FROM sales_rep WHERE sid=?);");
    ps.setInt(1, sid);
    ResultSet rs1=ps.executeQuery();
    if (rs.next() && rs1.next()) {
        String ss = rs.getString(1);
        int price = rs.getInt(2);
        Label companyname=new Label("Company Name: "+rs1.getString(1));
        Label snameLable = new Label("Sales-Rep. : "+ss);
        Label sprice = new Label("Price : "+price);
        Button makedealb = new Button("Make Deal");
        makedealb.setOnAction(e ->
            {
                try {
                    PreparedStatement pss=con.prepareStatement("SELECT * FROM deal WHERE cid=? AND sid=? AND product=? AND lead<>'win' AND lead<>'loss'");
                    pss.setInt(1, cid);
                    pss.setInt(2, sid);
                    pss.setString(3, product);
                    if(pss.executeQuery().next())
                    {
                   showAlert("You've already deal for it", "Failed", AlertType.INFORMATION);

                        return;
                    }
                    PreparedStatement ps1=con.prepareStatement("INSERT INTO deal (cid,sid,product,demand_count,lead)values(?,?,?,?,?)");
                    Statement st=con.createStatement();
                    ResultSet rs3=st.executeQuery("select demand_count from client where cid="+cid);
                    int demand_count=0;
                    if(rs3.next())
                    demand_count=rs3.getInt(1);
                    ps1.setInt(1, cid);
                    ps1.setInt(2, sid);
                    ps1.setString(3, product);
                    ps1.setInt(4,demand_count);
                    ps1.setString(5, "talking");
                    ps1.executeUpdate();
                    PreparedStatement ps2=con.prepareStatement("INSERT INTO leads values(?,?,?,?)");
                    ResultSet rs2=st.executeQuery("select max(deal_id) from deal");
                    if(rs2.next()){}
                    ps2.setInt(1,rs2.getInt(1));
                    ps2.setBoolean(2, false);
                    ps2.setBoolean(3, false);
                    ps2.setString(4,"talking");
                    ps2.executeUpdate();
                   showAlert("You made a deal", "Done", AlertType.INFORMATION);
                   afterlogin(cid);
                   return;
                   
                } catch (Exception e1) {
                    
                    e1.printStackTrace();
                }
            }
        );
        
        Button backb=new Button("Back");
            backb.setOnAction(e ->
            {
                pstage.setTitle("Home Page");
                pstage.setScene(homeScene);
            });
        GridPane grid=new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(companyname, 0, 0);
        grid.add(productLabel,0,1);
        grid.add(sprice, 0, 2);
        grid.add(snameLable,0,3);
        grid.add(makedealb, 0, 4);
        grid.add(backb,0,5);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
       ColumnConstraints col2 = new ColumnConstraints();
       col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        makedealScene=new Scene(grid,350,250);
        makedealScene.getStylesheets().add(getClass().getResource("stylegroup.css").toExternalForm());

        pstage.setTitle("Deals");
        pstage.setScene(makedealScene);
        
    }
}
Scene nextleadScene;
    void nextlead(int cid, int sid, String product) throws Exception {
    Label productLabel = new Label("Product: " + product);
    ResultSet rs = (con.createStatement()).executeQuery("SELECT sname FROM sales_rep WHERE sid=" + sid);
    rs.next();
    Label snameLabel = new Label("Sales-rep: " + rs.getString(1));
    
    ps = con.prepareStatement("SELECT lead FROM deal WHERE cid=? AND sid=? AND product=? AND lead<>'win' AND lead<>'loss'");
    ps.setInt(1, cid);
    ps.setInt(2, sid);
    ps.setString(3, product);
    ResultSet rs1 = ps.executeQuery();
    rs1.next();
    String currentStage = rs1.getString(1);
    Label stageLabel = new Label("Current Stage: " + currentStage);
    
    Button gonextb = new Button("Next Stage");
    Button rejectb = new Button("Reject");
    Button back = new Button("Back");

    gonextb.setOnAction(e -> {
        try {
            //PreparedStatement ps1 = con.prepareStatement("UPDATE leads SET cconf=1 WHERE deal_id IN(SELECT max(deal_id) FROM deal WHERE cid=? AND sid=? AND product=?)");
            CallableStatement ps1=con.prepareCall("{call update_lead_stage(?,?,?)}");
            ps1.setInt(1, cid);
            ps1.setInt(2, sid);
            ps1.setString(3, product);
            ps1.executeUpdate();
            nextlead(cid, sid, product);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
    });

    rejectb.setOnAction(e -> {
        try {
            con.setAutoCommit(false);
            PreparedStatement ps1=con.prepareStatement("SELECT max(deal_id) FROM deal WHERE cid=? AND sid=? AND product=?");
            
            ps1.setInt(1, cid);
            ps1.setInt(2, sid);
            ps1.setString(3, product);
           ResultSet getdealrs= ps1.executeQuery();
           getdealrs.next();
            PreparedStatement rejectps = con.prepareStatement("UPDATE leads SET lead='loss' WHERE deal_id = "+getdealrs.getInt(1));
            rejectps.executeUpdate();
            if( showConfirmationDialog("Warning", "Cancelling Deal", "You are going to cancel deal"))
            con.commit();
            else
            con.rollback();

            con.setAutoCommit(true);
            showAlert("Deal Rejected", "NOTICE", AlertType.INFORMATION);
            Thread.sleep(1000);
            //back.fire();
            afterlogin(cid);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    });

    back.setOnAction(e -> {
        pstage.setTitle("Home Page");
        pstage.setScene(homeScene);
    });

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 20, 20));
    
    grid.add(productLabel, 0, 0);
    grid.add(snameLabel, 0, 1);
    grid.add(stageLabel, 0, 2);
    grid.add(gonextb, 0, 3);
    grid.add(rejectb, 1, 3);
    grid.add(back, 0, 4);

    GridPane.setHalignment(gonextb, HPos.LEFT);
    GridPane.setHalignment(rejectb, HPos.RIGHT);
    GridPane.setHalignment(back, HPos.CENTER);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(50);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPercentWidth(50);
    grid.getColumnConstraints().addAll(col1, col2);

    nextleadScene = new Scene(grid, 350, 200);
    nextleadScene.getStylesheets().add(getClass().getResource("stylegroup.css").toExternalForm());

    pstage.setTitle("Leads");
    pstage.setScene(nextleadScene);
}

    private void showAlert(String message, String title, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
    
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
}
class BST
{
    class Node
    {
        String product;
        String sname;
        int price;
        int sid;
        Node left, right;
        Node(String sname,String product,int price,int sid)
        {
            this.product = product;
            this.sname=sname;
            this.price=price;
            this.sid=sid;
            left = null;
            right=null;
        }
        @Override
        public String toString() {
            return "Product: " + product + " Sales-rep: " + sname + " Price: " + price;
        }
        public String getProduct() {
            return product;
        }
        public String getSname() {
            return sname;
        }
        public int getPrice() {
            return price;
        }
        public int getSid() {
            return sid;
        }
       
    }
    Node root = null;

    //By Product
    void insertByPro(String sname,String product,int price,int sid)
    {
        root = insertpro(root,sname,product,price,sid);
       
    }
    Node insertpro(Node root, String sname,String product,int price,int sid) {
        Node n = new Node(sname, product,price,sid);
        if (root == null) {
            root = n;
            return root;
        }
        if (root.product.compareTo(product) >= 0) {
            root.left = insertpro(root.left,sname,product,price,sid);
        } else if (root.product.compareTo(product) < 0) {
            root.right = insertpro(root.right, sname,product,price,sid);
        }
        return root;
    }

    // for printing in terminal
    public void printInOrder() {
        printInOrderRec(root);
    }

    private void printInOrderRec(Node root) {
        if (root != null) {
            printInOrderRec(root.left);
            System.out.println("Product: " + root.product + ", SID: " + root.sid + ", Snamee: " + root.sname);
            printInOrderRec(root.right);
        }
    }

    // Search for nodes containing the query string
    public ObservableList<Node> search(String query) {
        ObservableList<Node> results = FXCollections.observableArrayList();
        searchRec(root, query.toLowerCase(), results);
        return results;
    }

    private void searchRec(Node root, String query, ObservableList<Node> results) {
        if (root != null) {
            if (root.product.toLowerCase().contains(query)) {
                results.add(root);
            }
            if (root.product.toLowerCase().compareTo(query) >= 0) {
                searchRec(root.left, query, results);
            }
            if (root.product.toLowerCase().compareTo(query) <= 0) {
                searchRec(root.right, query, results);
            }
        }
    }
}

class ll1 //Linked List
{
    class node
    {
        int sid;
        String sname;
        String product;
        node next;
        node(int sid, String sname, String product) {
            this.sid = sid;
            this.sname = sname;
            this.product = product;
            this.next = null;
        }
        @Override
        public String toString() {
            return "Product: "+product+" Sales-rep: "+sname;
        }
        public int getSid() {
            return sid;
        }
        public String getSname() {
            return sname;
        }
        public String getProduct() {
            return product;
        }
        
        
    }
    node first=null;
    node insert(int sid, String sname,String product) {
        node newNode = new node(sid, sname, product);
        if (first == null) {
            first = newNode;
            
        } else {
            node current = first;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        return newNode;
    }
}
