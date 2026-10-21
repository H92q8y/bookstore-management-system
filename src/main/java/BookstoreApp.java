import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BookstoreApp extends Application
{

    private Stage primaryStage;

    private TableView<Book> table;

    private ObservableList<Book> masterBooks = FXCollections.observableArrayList();
    private ObservableList<Book> shownBooks  = FXCollections.observableArrayList();

    private String currentRole;
    private String currentUsername;

    @Override
    public void start(Stage stage)
    {
        this.primaryStage = stage;
        showLoginScreen();
        primaryStage.show();
    }

    private Scene createStyledScene(Parent root, double width, double height)
    {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("bookstore.css").toExternalForm());
        return scene;
    }



    //login
    private void showLoginScreen()
    {
        currentRole = null;
        currentUsername = null;

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Bookstore Login");

        TextField userField = new TextField();
        userField.setPromptText("Username (example: admin)");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (example: admin123)");

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (user.isEmpty() || pass.isEmpty())
            {
                showAlert(Alert.AlertType.WARNING, "Input error", "Please enter username and password.");
                return;
            }

            String role = DatabaseHandler.getUserRole(user, pass);
            if (role != null)
            {
                showDashboard(role, user);
            }
            else
            {
                showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
            }
        });

        registerBtn.setOnAction(e -> showRegisterWindow(userField, passField));

        VBox buttons = new VBox(8, loginBtn, registerBtn);

        layout.getChildren().addAll(title, userField, passField, buttons);

        primaryStage.setTitle("Login");
        primaryStage.setScene(createStyledScene(layout, 340, 260));
    }



    // REGISTER
    private void showRegisterWindow(TextField loginUserField, PasswordField loginPassField) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Register New User");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TextField userField = new TextField();
        userField.setPromptText("New username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("New password");

        PasswordField pass2Field = new PasswordField();
        pass2Field.setPromptText("Repeat password");

        Button createBtn = new Button("Create Account");
        Button cancelBtn = new Button("Cancel");

        createBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p1 = passField.getText().trim();
            String p2 = pass2Field.getText().trim();

            if (u.isEmpty() || p1.isEmpty() || p2.isEmpty())
            {
                showAlert(Alert.AlertType.WARNING, "Input error", "All fields are required.");
                return;
            }

            if (!p1.equals(p2))
            {
                showAlert(Alert.AlertType.WARNING, "Input error", "Passwords do not match.");
                return;
            }

            boolean created = DatabaseHandler.registerUser(u, p1);
            if (created)
            {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created! You can login now.");

                // put values into login fields
                loginUserField.setText(u);
                loginPassField.setText(p1);

                dialog.close();
            }
            else
            {
                showAlert(Alert.AlertType.ERROR, "Error", "Username already exists (or database error).");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(
                new Label("Create a new account (role = User):"),
                userField, passField, pass2Field,
                new HBox(10, createBtn, cancelBtn)
        );

        dialog.setScene(createStyledScene(root, 320, 250));
        dialog.showAndWait();
    }



      // screen 2: DASHBOARD
    private void showDashboard(String role, String username)
    {
        this.currentRole = role;
        this.currentUsername = username;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label topLabel = new Label("Logged in as: " + username + " (" + role + ")");
        root.setTop(topLabel);

        table = new TableView<>();
        createBookColumns(table);

        refreshTable();

        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(10));
        rightMenu.setPrefWidth(230);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshTable());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> showLoginScreen());

        if ("Admin".equalsIgnoreCase(role))
        {

            Button addBtn = new Button("Add Book");
            addBtn.setMaxWidth(Double.MAX_VALUE);
            addBtn.setOnAction(e -> showAddBookWindow());


            Button editBtn = new Button("Edit Selected");
            editBtn.setMaxWidth(Double.MAX_VALUE);
            editBtn.setOnAction(e -> {
                Book selected = table.getSelectionModel().getSelectedItem();
                if (selected == null)
                {
                    showAlert(Alert.AlertType.WARNING, "No selection", "Please select a book in the table.");
                    return;
                }
                showEditBookWindow(selected);
            });

            Button deleteBtn = new Button("Delete Selected");
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setOnAction(e -> {
                Book selected = table.getSelectionModel().getSelectedItem();
                if (selected == null)
                {
                    showAlert(Alert.AlertType.WARNING, "No selection", "Please select a book in the table.");
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete book: " + selected.getTitle() + " ?",
                        ButtonType.OK, ButtonType.CANCEL);

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                {
                    DatabaseHandler.deleteBook(selected.getId());
                    refreshTable();
                }
            });

            Button manageUsersBtn = new Button("Manage Users");
            manageUsersBtn.setMaxWidth(Double.MAX_VALUE);
            manageUsersBtn.setOnAction(e -> showUsersWindow());

            rightMenu.getChildren().addAll(
                    new Label("Admin Controls:"),
                    addBtn, editBtn, deleteBtn, manageUsersBtn,
                    refreshBtn, logoutBtn
            );

        }
        else
        {
            // User functionality

            Button searchBtn = new Button("Search Books");
            searchBtn.setMaxWidth(Double.MAX_VALUE);
            searchBtn.setOnAction(e -> showSearchWindow());

            Button showAllBtn = new Button("Show All Books");
            showAllBtn.setMaxWidth(Double.MAX_VALUE);
            showAllBtn.setOnAction(e -> shownBooks.setAll(masterBooks));

            Button buyBtn = new Button("Buy Selected");
            buyBtn.setMaxWidth(Double.MAX_VALUE);
            buyBtn.setOnAction(e -> {
                Book selected = table.getSelectionModel().getSelectedItem();
                if (selected == null)
                {
                    showAlert(Alert.AlertType.WARNING, "No selection", "Please select a book in the table.");
                    return;
                }
                showBuyWindow(selected);
            });

            rightMenu.getChildren().addAll(
                    new Label("User Controls:"),
                    searchBtn, showAllBtn, buyBtn,
                    refreshBtn, logoutBtn
            );
        }

        root.setCenter(table);
        root.setRight(rightMenu);

        primaryStage.setTitle("Bookstore Dashboard");
        primaryStage.setScene(createStyledScene(root, 900, 460));
    }

    private void createBookColumns(TableView<Book> anyTable)
    {
        anyTable.getColumns().clear();

        TableColumn<Book, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(220);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAuthor.setPrefWidth(180);

        TableColumn<Book, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<Book, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(120);

        anyTable.getColumns().addAll(colId, colTitle, colAuthor, colPrice, colQty);
    }

    private void refreshTable()
    {
        masterBooks.setAll(DatabaseHandler.getAllBooks());
        shownBooks.setAll(masterBooks);
        table.setItems(shownBooks);
    }



     // SEARCH    (User)
    private void showSearchWindow()
    {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Search Books");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox top = new HBox(10);
        TextField queryField = new TextField();
        queryField.setPromptText("Type title or author...");

        Button searchBtn = new Button("Search");
        Button clearBtn = new Button("Clear");

        top.getChildren().addAll(queryField, searchBtn, clearBtn);
        root.setTop(top);

        TableView<Book> resultsTable = new TableView<>();
        createBookColumns(resultsTable);

        ObservableList<Book> results = FXCollections.observableArrayList(masterBooks);
        resultsTable.setItems(results);

        root.setCenter(resultsTable);

        Button applyBtn = new Button("Apply Results to Main Table");
        Button closeBtn = new Button("Close");

        applyBtn.setOnAction(e -> {
            shownBooks.setAll(results);
            dialog.close();
        });

        closeBtn.setOnAction(e -> dialog.close());

        HBox bottom = new HBox(10, applyBtn, closeBtn);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottom);

        searchBtn.setOnAction(e -> {
            String q = queryField.getText().trim().toLowerCase();
            results.clear();

            if (q.isEmpty())
            {
                results.setAll(masterBooks);
                return;
            }

            for (Book b : masterBooks)
            {
                String t = (b.getTitle() == null) ? "" : b.getTitle().toLowerCase();
                String a = (b.getAuthor() == null) ? "" : b.getAuthor().toLowerCase();
                if (t.contains(q) || a.contains(q))
                {
                    results.add(b);
                }
            }
        });

        clearBtn.setOnAction(e -> {
            queryField.clear();
            results.setAll(masterBooks);
        });

        dialog.setScene(createStyledScene(root, 850, 420));
        dialog.showAndWait();
    }



    //ADD BOOK (Admin)
    private void showAddBookWindow()
    {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Book");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TextField tField = new TextField();
        tField.setPromptText("Title");

        TextField aField = new TextField();
        aField.setPromptText("Author");

        TextField pField = new TextField();
        pField.setPromptText("Price (example: 29.99)");

        TextField qField = new TextField();
        qField.setPromptText("Quantity (example: 10)");

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setOnAction(e -> {
            try {
                String title = tField.getText().trim();
                String author = aField.getText().trim();
                double price = Double.parseDouble(pField.getText().trim());
                int qty = Integer.parseInt(qField.getText().trim());

                if (title.isEmpty() || author.isEmpty())
                {
                    showAlert(Alert.AlertType.WARNING, "Input error", "Title and Author must not be empty.");
                    return;
                }
                if (qty < 0 || price < 0)
                {
                    showAlert(Alert.AlertType.WARNING, "Input error", "Price and Quantity must be >= 0.");
                    return;
                }

                DatabaseHandler.addBook(title, author, price, qty);
                refreshTable();
                dialog.close();

            }
            catch (NumberFormatException ex)
            {
                showAlert(Alert.AlertType.ERROR, "Input error", "Wrong number format for price or quantity.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(
                new Label("Enter book data:"),
                tField, aField, pField, qField,
                new HBox(10, saveBtn, cancelBtn)
        );

        dialog.setScene(createStyledScene(root, 340, 290));
        dialog.showAndWait();
    }


    //EDIT BOOK   (Admin)
    private void showEditBookWindow(Book book) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Book (ID=" + book.getId() + ")");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TextField tField = new TextField(book.getTitle());
        TextField aField = new TextField(book.getAuthor());
        TextField pField = new TextField(String.valueOf(book.getPrice()));
        TextField qField = new TextField(String.valueOf(book.getQuantity()));

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setOnAction(e -> {
            try
            {
                String title = tField.getText().trim();
                String author = aField.getText().trim();
                double price = Double.parseDouble(pField.getText().trim());
                int qty = Integer.parseInt(qField.getText().trim());

                if (title.isEmpty() || author.isEmpty())
                {
                    showAlert(Alert.AlertType.WARNING, "Input error", "Title and Author must not be empty.");
                    return;
                }
                if (qty < 0 || price < 0)
                {
                    showAlert(Alert.AlertType.WARNING, "Input error", "Price and Quantity must be >= 0.");
                    return;
                }

                DatabaseHandler.updateBook(book.getId(), title, author, price, qty);
                refreshTable();
                dialog.close();

            }
            catch (NumberFormatException ex)
            {
                showAlert(Alert.AlertType.ERROR, "Input error", "Wrong number format for price or quantity.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(
                new Label("Edit book data:"),
                new Label("Title:"), tField,
                new Label("Author:"), aField,
                new Label("Price:"), pField,
                new Label("Quantity:"), qField,
                new HBox(10, saveBtn, cancelBtn)
        );

        dialog.setScene(createStyledScene(root, 360, 380));
        dialog.showAndWait();
    }




    //BUY BOOK
    private void showBuyWindow(Book book)
    {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Buy Book");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label info = new Label("Book: " + book.getTitle() + "\nIn stock: " + book.getQuantity());

        TextField qtyField = new TextField();
        qtyField.setPromptText("How many to buy? (example: 1)");

        Button buyBtn = new Button("Buy");
        Button cancelBtn = new Button("Cancel");

        buyBtn.setOnAction(e -> {
            try
            {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0)
                {
                    showAlert(Alert.AlertType.WARNING, "Input error", "Quantity must be > 0.");
                    return;
                }

                boolean ok = DatabaseHandler.buyBook(book.getId(), qty);
                if (ok)
                {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Purchase completed!");
                    refreshTable();
                    dialog.close();
                }
                else
                {
                    showAlert(Alert.AlertType.ERROR, "Error", "Not enough stock.");
                }

            }
            catch (NumberFormatException ex)
            {
                showAlert(Alert.AlertType.ERROR, "Input error", "Please enter a number.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(info, qtyField, new HBox(10, buyBtn, cancelBtn));

        dialog.setScene(createStyledScene(root, 340, 190));
        dialog.showAndWait();
    }




        //WINDOW - MANAGE USERs
    private void showUsersWindow()
    {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Manage Users");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        TableView<AppUser> usersTable = new TableView<>();

        TableColumn<AppUser, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsername.setPrefWidth(220);

        TableColumn<AppUser, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(120);

        usersTable.getColumns().addAll(colUsername, colRole);
        usersTable.setItems(DatabaseHandler.getAllUsers());

        root.setCenter(usersTable);

        Button deleteUserBtn = new Button("Delete Selected");
        Button refreshBtn = new Button("Refresh");
        Button closeBtn = new Button("Close");

        deleteUserBtn.setOnAction(e -> {
            AppUser selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null)
            {
                showAlert(Alert.AlertType.WARNING, "No selection", "Please select a user.");
                return;
            }

            if (selected.getUsername().equals(currentUsername))
            {
                showAlert(Alert.AlertType.WARNING, "Not allowed", "You cannot delete your own account.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete user: " + selected.getUsername() + " ?",
                    ButtonType.OK, ButtonType.CANCEL);

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
            {
                DatabaseHandler.deleteUser(selected.getUsername());
                usersTable.setItems(DatabaseHandler.getAllUsers());
            }
        });

        refreshBtn.setOnAction(e -> usersTable.setItems(DatabaseHandler.getAllUsers()));
        closeBtn.setOnAction(e -> dialog.close());

        VBox bottom = new VBox(10,
                new Label("Users list (view/delete only):"),
                new HBox(10, deleteUserBtn, refreshBtn, closeBtn)
        );
        bottom.setPadding(new Insets(10));

        root.setBottom(bottom);

        dialog.setScene(createStyledScene(root, 480, 380));
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String text)
    {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}