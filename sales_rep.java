import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
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

public class sales_rep extends Application {
   Connection con;
    PreparedStatement ps;
    Statement stmt;
    Scene loginScene;
    Stage pstage;
    static BST2 bst;
    public static void main(String[] args)throws Exception {
        
        launch(args); 
    }

    @Override
    public void start(Stage pstage) throws Exception {
        this.pstage=pstage;
        bst=new BST2();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/group", "root", "");
        Statement st=conn.createStatement();
        ResultSet rst=st.executeQuery("SELECT cname,demand,demand_count,cid FROM client;");
        while(rst.next())
        {
            bst.insertByPro(rst.getString(1), rst.getString(2), rst.getInt(3),rst.getInt(4));
        }
       // bst.printInOrder();

        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/group", "root", "");
        
       Label loginLabel=new Label("Enter Sales Representative Id:");
        TextField idtf=new TextField();
        Label passLabel=new Label("Enter Password:");
        PasswordField passf=new PasswordField();
        Button loginb=new Button("Login");
        Label blankLabel=new Label();
        Button back=new Button("Back");

        loginb.setOnAction(e ->
        {
            int sid=Integer.parseInt(idtf.getText());
            try {
                ps=con.prepareStatement("select pass from spass where sid="+sid);
                ResultSet rs=ps.executeQuery();
                if(rs.next())
                {
                    if(passf.getText().equals(rs.getString(1)))
                    {
                        afterlogin(sid);
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
        back.setOnAction(e ->{
            new maingroup().start(pstage);
        });
        back.getStyleClass().add("back-button");
        VBox v=new VBox(10,loginLabel,idtf,passLabel,passf,loginb,back);
        v.setAlignment(Pos.CENTER);
        v.setStyle("-fx-padding: 20;");
        loginScene=new Scene(v,350,300);
        loginScene.getStylesheets().add(getClass().getResource("stylegroup.css").toExternalForm());
        
        pstage.setTitle("LOGIN");
        pstage.setScene(loginScene);
        pstage.show();
    }
 Scene homeScene;
    void afterlogin(int sid) throws Exception
    {
        
        Label itemsLabel=new Label("Items on Sale:");
        TextArea itemsta=new TextArea();
        ps=con.prepareStatement("select products,price from sales_rep where sid="+sid);
        ResultSet rs=ps.executeQuery();
        while(rs.next())
        {
            String demand=rs.getString(1);
            int demand_count=rs.getInt(2);
            itemsta.appendText(demand+" "+demand_count+"\n");
        }
        itemsta.setEditable(false);
        Label dealsta=new Label("Current Deals:");
        ListView<ll2.node> dealsListView = new ListView<>();
        ObservableList<ll2.node> dealsList = FXCollections.observableArrayList();

        
            String sql1 = "SELECT deal.product, deal.demand_count, client.cname,deal.cid FROM DEAL INNER JOIN client ON deal.cid = client.cid WHERE lead<>'loss';";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            ll2 l=new ll2();
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
                        String selectedDeal = dealsListView.getSelectionModel().getSelectedItem().demand;
                        if (selectedDeal != null) {
                        nextlead(sid,dealsListView.getSelectionModel().getSelectedItem().cid, dealsListView.getSelectionModel().getSelectedItem().demand);
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
        searchField.setPromptText("Search...");
        Label searchlabel=new Label("Search:");
        // Create Button to trigger the search
        Button searchButton = new Button("Search");

        // Create ListView to display search results
        ListView<BST2.Node> resultsListView = new ListView<>();
        resultsListView.setItems(bst.search(""));

        // Add action to the button to perform the search
        searchButton.setOnAction(event -> {
            String query = searchField.getText();
            ObservableList<BST2.Node> results = bst.search(query);
            resultsListView.setItems(results);
        });

        // Handle item clicks to open a new page
        resultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // double-click
                BST2.Node selectedItem = resultsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                   try {
                    
                    makedeal(sid,selectedItem.cid,selectedItem.demand);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }
                }
                
            }
        });

        Button editb=new Button("Edit");
        editb.getStyleClass().add("edit-button");
        editb.setOnAction(e -> {
            try {
                editdemand(sid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });


        Label reportLabel=new Label("Generate Report: ");
        Button reportb=new Button("Download");

        reportb.setOnAction(e ->
        {
            
            try {
                PreparedStatement getcname=con.prepareStatement("SELECT sname,LOCALTIMESTAMP FROM sales_rep WHERE sid="+sid);
                ResultSet getcnamers=getcname.executeQuery();
                getcnamers.next();
                String sname1=getcnamers.getString(1);
                String sname=""+sname1+getcnamers.getString(2).replace(":", "-").replace(".", "-");
                File f=new File("C:\\Users\\rmp20\\Downloads\\"+sname +".txt");
                f.createNewFile();
                BufferedWriter bw=new BufferedWriter(new FileWriter(f));
                bw.write("NAME: "+sname1);
                bw.newLine();
                bw.write("Sales Rep. ID: "+sid+"\n \n");
                bw.write("~~ Your Items ~~\n");
                ps=con.prepareStatement("SELECT products,price FROM sales_rep WHERE sid="+sid);
                ResultSet rs2=ps.executeQuery();
                while(rs2.next())
                {
                    bw.write(rs2.getString(1)+" "+rs2.getInt(2)+"\n");
                }
                bw.newLine();
                bw.write("~~ Total Deals ~~\n");
                stmt=con.createStatement();
                ResultSet rs3=stmt.executeQuery("SELECT cid,product,lead FROM deal WHERE sid="+sid);
                int i=0;
                while (rs3.next()) {
                    int cid1=rs3.getInt(1);
                    String demand1=rs3.getString(2);
                    String lead1=rs3.getString(3);
                     ps=con.prepareStatement("SELECT cname,demand_count FROM client WHERE cid=? AND demand=?");
                     ps.setInt(1, cid1);
                     ps.setString(2, demand1);
                     ResultSet rs4=ps.executeQuery();
                     rs4.next();
                     String cname1=rs4.getString(1);
                     int demand_count=rs4.getInt(2);
                    bw.write((++i)+") Client Name. = "+cname1+" Demand = "+demand1+" Demand_count = "+demand_count+" Stage = "+lead1);
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
        grid.add(itemsLabel, 0, 0);
        grid.add(itemsta, 1, 0);
        grid.add(editb, 2, 0);
        grid.add(dealsta, 0, 1);
        grid.add(dealsListView, 1, 1);
        HBox h=new HBox(10,reportLabel,reportb);
        grid.add(h,1,2);
        grid.add(searchlabel,0,3);
        grid.add(searchField, 0, 4);        
        grid.add(searchButton, 1, 4);
        grid.add(resultsListView, 0, 5);
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
void makedeal(int cid,int sid,String demand) throws Exception{
    Label demandLabel = new Label("Demand : "+ demand);
    PreparedStatement pn = con.prepareStatement("select cname,demand_count from client where cid = "+cid+" and demand='"+demand+"';");
    ResultSet rs=pn.executeQuery();
    rs.next();
    String cname=rs.getString(1);
    int demand_count=rs.getInt(2);
    Label cnameLable=new Label("Client Name: "+cname);
    Label demand_countLabel=new Label("Demand Count: "+demand_count);
        Button makedealb = new Button("Make Deal");
        makedealb.setOnAction(e ->
            {
                try {
                    PreparedStatement pss=con.prepareStatement("SELECT * FROM deal WHERE cid=? AND sid=? AND product=? AND lead<>'win' AND lead<>'loss'");
                    pss.setInt(1, cid);
                    pss.setInt(2, sid);
                    pss.setString(3, demand);
                    if(pss.executeQuery().next())
                    {
                        showAlert("You've already deal for it", "Failed", AlertType.INFORMATION);
                        return;
                    }
                    PreparedStatement ps1=con.prepareStatement("INSERT INTO deal (cid,sid,product,demand_count,lead)values(?,?,?,?,?)");
                    Statement st=con.createStatement();
                    ps1.setInt(1, cid);
                    ps1.setInt(2, sid);
                    ps1.setString(3, demand);
                    ps1.setInt(4,demand_count);
                    ps1.setString(5, "talking");
                    ps1.executeUpdate();
                    PreparedStatement ps2=con.prepareStatement("INSERT INTO leads values(?,?,?,?)");
                    ResultSet rs2=st.executeQuery("select max(deal_id) from deal");
                    rs2.next();
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
        grid.add(cnameLable, 0, 0);
        grid.add(demandLabel,0,1);
        grid.add(demand_countLabel, 0, 2);
        grid.add(makedealb, 0, 3);
        grid.add(backb,0,4);

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
Scene nextleadScene;
    void nextlead(int sid, int cid, String demand) throws Exception {
    Label demandLabel = new Label("Demand: " + demand);
    ResultSet rs = (con.createStatement()).executeQuery("SELECT cname FROM client WHERE cid=" + cid);
    rs.next();
    Label cnameLabel = new Label("Client Name: " + rs.getString(1));
    
    ps = con.prepareStatement("SELECT lead FROM deal WHERE cid=? AND sid=? AND product=? AND lead<>'win' AND lead<>'loss'");
    ps.setInt(1, cid);
    ps.setInt(2, sid);
    ps.setString(3, demand);
    ResultSet rs1 = ps.executeQuery();
    rs1.next();
    String currentStage = rs1.getString(1);
    Label stageLabel = new Label("Current Stage: " + currentStage);
    
    Button gonextb = new Button("Next Stage");
    Button rejectb = new Button("Reject");
    Button back = new Button("Back");

    gonextb.setOnAction(e -> {
        try {
            //PreparedStatement ps1 = con.prepareStatement("UPDATE leads SET cconf=1 WHERE deal_id IN(SELECT max(deal_id) FROM deal WHERE cid=? AND sid=? AND demand=?)");
            CallableStatement ps1=con.prepareCall("{call update_lead_s(?,?,?)}");
            ps1.setInt(1, cid);
            ps1.setInt(2, sid);
            ps1.setString(3, demand);
            ps1.executeUpdate();
            nextlead(cid, sid, demand);
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
            ps1.setString(3, demand);
           ResultSet getdealrs= ps1.executeQuery();
           getdealrs.next();
            PreparedStatement rejectps = con.prepareStatement("UPDATE leads SET lead='loss' WHERE deal_id = "+getdealrs.getInt(1));
            rejectps.executeUpdate();
            if( showConfirmationDialog("Warning", "Cancelling Deal", "You are going to cancel deal"))
            {con.commit();
            con.setAutoCommit(true);
            showAlert("Deal Rejected", "NOTICE", AlertType.INFORMATION);
            Thread.sleep(1000);
            //back.fire();
            afterlogin(cid);
            }else
            {
                 con.rollback();
            }
           

            
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
    
    grid.add(demandLabel, 0, 0);
    grid.add(cnameLabel, 0, 1);
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
class BST2
{
    class Node
    {
        String demand;
        String cname;
        int demand_count;
        int cid;
        Node left, right;
        Node(String cname,String demand,int demand_count,int cid)
        {
            this.demand = demand;
            this.cname=cname;
            this.demand_count=demand_count;
            this.cid=cid;
            left = null;
            right=null;
        }
        @Override
        public String toString() {
            return "cid=" + cid + ", cname=" + cname + ", demand="+demand+", demand_count=" + demand_count;
        }
        public String getDemand() {
            return demand;
        }
        public String getCname() {
            return cname;
        }
        public int getDemand_count() {
            return demand_count;
        }
        public int getCid() {
            return cid;
        }
        
    }
    Node root = null;

    //By demand
    void insertByPro(String cname,String demand,int demand_count,int cid)
    {
        root = insertpro(root,cname,demand,demand_count,cid);
       
    }
    Node insertpro(Node root, String cname,String demand,int demand_count,int sid) {
        Node n = new Node(cname, demand,demand_count,sid);
        if (root == null) {
            root = n;
            return root;
        }
        if (root.demand.compareTo(demand) >= 0) {
            root.left = insertpro(root.left,cname,demand,demand_count,sid);
        } else if (root.demand.compareTo(demand) < 0) {
            root.right = insertpro(root.right, cname,demand,demand_count,sid);
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
            System.out.println("demand: " + root.demand + ", SID: " + root.cid + ", cnamee: " + root.cname);
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
            if (root.demand.toLowerCase().contains(query)) {
                results.add(root);
            }
            if (root.demand.toLowerCase().compareTo(query) >= 0) {
                searchRec(root.left, query, results);
            }
            if (root.demand.toLowerCase().compareTo(query) <= 0) {
                searchRec(root.right, query, results);
            }
        }
    }
}

class ll2 //Linked List
{
    class node
    {
        int cid;
        String cname;
        String demand;
        node next;
        node(int cid, String cname, String demand) {
            this.cid = cid;
            this.cname = cname;
            this.demand = demand;
            this.next = null;
        }
        @Override
        public String toString() {
            return "demand: "+demand+" Sales-rep: "+cname;
        }
        public int getCid() {
            return cid;
        }
        public String getcname() {
            return cname;
        }
        public String getDemand() {
            return demand;
        }
        
        
    }
    node first=null;
    node insert(int cid, String cname,String demand) {
        node newNode = new node(cid, cname, demand);
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
