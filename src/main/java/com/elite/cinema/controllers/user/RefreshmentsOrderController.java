package com.elite.cinema.controllers.user;

import atlantafx.base.theme.Styles;
import com.elite.cinema.controllers.MainController;
import com.elite.cinema.models.tables.pojos.Refreshments;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.RefreshmentsOrderDetails;
import com.elite.cinema.utils.ImageHelper;
import com.elite.cinema.utils.PriceFormatter;
import com.elite.cinema.utils.RefreshmentReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elite.cinema.models.Tables.REFRESHMENTS_ORDERS;

public class RefreshmentsOrderController extends MainController {

    @FXML
    private FlowPane refreshmentsContainer;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label taxLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Button clearCartButton;

    @FXML
    private Button checkoutButton;

    private final ObservableList<Refreshments> refreshmentsList = FXCollections.observableArrayList();
    private final Map<ULong, Integer> cart = new HashMap<>();
    private final Map<ULong, Node> cartNodes = new HashMap<>();

    public void initialize()
    {
        clearCartButton.getStyleClass().add(Styles.DANGER);
        checkoutButton.getStyleClass().add(Styles.SUCCESS);
    }

    @Override
    public void ready(Object params) {
        loadRefreshments();
        updateOrderSummary();
    }

    private void loadRefreshments() {
        refreshmentsContainer.getChildren().clear();
        refreshmentsList.clear();

        // Load all refreshments from database
        List<Refreshments> allRefreshments = DbSet.refreshments().findAll();
        refreshmentsList.addAll(allRefreshments);

        // Create refreshment items and add to container
        for (Refreshments refreshment : refreshmentsList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/user/refreshment-item.fxml"));
                VBox refreshmentItem = loader.load();

                // Set refreshment data
                ImageView refreshmentImage = (ImageView) refreshmentItem.lookup("#refreshmentImage");
                Label nameLabel = (Label) refreshmentItem.lookup("#nameLabel");
                Label priceLabel = (Label) refreshmentItem.lookup("#priceLabel");
                Label quantityLabel = (Label) refreshmentItem.lookup("#quantityLabel");
                Button minusButton = (Button) refreshmentItem.lookup("#minusButton");
                Button plusButton = (Button) refreshmentItem.lookup("#plusButton");

                // Set text values
                nameLabel.setText(refreshment.getName());
                priceLabel.setText(PriceFormatter.format(refreshment.getPrice().longValue()));
                quantityLabel.setText(cart.getOrDefault(refreshment.getId(), 0).toString());

                // Load image if available
                if (refreshment.getImage() != null) {
                    try {
                        refreshmentImage.setImage(ImageHelper.byteArrayToImage(refreshment.getImage()));
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                    }
                }

                // Set button actions
                minusButton.setOnAction(_ -> decreaseQuantity(refreshment, quantityLabel));
                plusButton.setOnAction(_ -> increaseQuantity(refreshment, quantityLabel));

                refreshmentsContainer.getChildren().add(refreshmentItem);
            } catch (IOException e) {
                System.err.println("Error loading refreshment item: " + e.getMessage());
            }
        }
    }

    private void increaseQuantity(Refreshments refreshment, Label quantityLabel) {
        int currentQuantity = cart.getOrDefault(refreshment.getId(), 0);
        cart.put(refreshment.getId(), currentQuantity + 1);
        quantityLabel.setText(String.valueOf(currentQuantity + 1));
        updateCart(refreshment);
        updateOrderSummary();
    }

    private void decreaseQuantity(Refreshments refreshment, Label quantityLabel) {
        int currentQuantity = cart.getOrDefault(refreshment.getId(), 0);
        if (currentQuantity > 0) {
            cart.put(refreshment.getId(), currentQuantity - 1);
            quantityLabel.setText(String.valueOf(currentQuantity - 1));
            updateCart(refreshment);
            updateOrderSummary();
        }
    }

    private void updateCart(Refreshments refreshment) {
        int quantity = cart.getOrDefault(refreshment.getId(), 0);

        if (quantity <= 0) {
            // Remove from cart
            cart.remove(refreshment.getId());
            if (cartNodes.containsKey(refreshment.getId())) {
                cartItemsContainer.getChildren().remove(cartNodes.get(refreshment.getId()));
                cartNodes.remove(refreshment.getId());
            }
            return;
        }

        // Add or update cart item
        if (!cartNodes.containsKey(refreshment.getId())) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/user/cart-item.fxml"));
                HBox cartItem = loader.load();

                cartNodes.put(refreshment.getId(), cartItem);
                cartItemsContainer.getChildren().add(cartItem);

                // Set remove button action
                Button removeButton = (Button) cartItem.lookup("#removeButton");
                removeButton.setOnAction(_ -> removeFromCart(refreshment));

                // Load image if available
                ImageView itemImage = (ImageView) cartItem.lookup("#itemImage");
                // Load image if available
                if (refreshment.getImage() != null) {
                    try {
                        itemImage.setImage(ImageHelper.byteArrayToImage(refreshment.getImage()));
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                System.err.println("Error loading cart item: " + e.getMessage());
                return;
            }
        }

        // Update cart item
        HBox cartItem = (HBox) cartNodes.get(refreshment.getId());
        Label itemNameLabel = (Label) cartItem.lookup("#itemNameLabel");
        Label itemPriceLabel = (Label) cartItem.lookup("#itemPriceLabel");
        Label itemQuantityLabel = (Label) cartItem.lookup("#itemQuantityLabel");
        Label itemTotalLabel = (Label) cartItem.lookup("#itemTotalLabel");

        itemNameLabel.setText(refreshment.getName());
        itemPriceLabel.setText(PriceFormatter.format(refreshment.getPrice().longValue()));
        itemQuantityLabel.setText(String.valueOf(quantity));
        itemTotalLabel.setText(PriceFormatter.format(refreshment.getPrice().longValue() * quantity));
    }

    private void removeFromCart(Refreshments refreshment) {
        cart.remove(refreshment.getId());

        // Update quantity display in refreshment item
        for (Node node : refreshmentsContainer.getChildren()) {
            Label nameLabel = (Label) node.lookup("#nameLabel");
            if (nameLabel != null && nameLabel.getText().equals(refreshment.getName())) {
                Label quantityLabel = (Label) node.lookup("#quantityLabel");
                quantityLabel.setText("0");
                break;
            }
        }

        // Remove from cart display
        if (cartNodes.containsKey(refreshment.getId())) {
            cartItemsContainer.getChildren().remove(cartNodes.get(refreshment.getId()));
            cartNodes.remove(refreshment.getId());
        }

        updateOrderSummary();
    }

    private void updateOrderSummary() {
        long subtotal = calculateSubtotal();
        long tax = subtotal / 10; // 10% tax
        long total = subtotal + tax;

        subtotalLabel.setText(PriceFormatter.format(subtotal));
        taxLabel.setText(PriceFormatter.format(tax));
        totalLabel.setText(PriceFormatter.format(total));

        // Disable checkout button if cart is empty
        checkoutButton.setDisable(cart.isEmpty());
    }

    private long calculateSubtotal() {
        long subtotal = 0;
        for (var entry : cart.entrySet()) {
            ULong refreshmentId = entry.getKey();
            Integer quantity = entry.getValue();

            for (Refreshments refreshment : refreshmentsList) {
                if (refreshment.getId().equals(refreshmentId)) {
                    subtotal += refreshment.getPrice().longValue() * quantity;
                    break;
                }
            }
        }

        return subtotal;
    }

    @FXML
    private void onClearCart() {
        cart.clear();
        cartNodes.clear();
        cartItemsContainer.getChildren().clear();

        // Reset all quantity labels in refreshment items
        for (Node node : refreshmentsContainer.getChildren()) {
            Label quantityLabel = (Label) node.lookup("#quantityLabel");
            if (quantityLabel != null) {
                quantityLabel.setText("0");
            }
        }

        updateOrderSummary();
    }

    @FXML
    private void onCheckout() {
        if (cart.isEmpty()) {
            showAlert("Error", "Your cart is empty", Alert.AlertType.ERROR);
            return;
        }


        var order = DbSet.getContext().newRecord(REFRESHMENTS_ORDERS);
        order.setTotal(ULong.valueOf(calculateSubtotal() + calculateSubtotal() / 10));
        order.insert();

        var refreshments = cart.entrySet().stream()
                .map(entry -> new RefreshmentsOrderDetails(
                        order.getId(),
                        entry.getKey(),
                        UInteger.valueOf(entry.getValue())
                ))
                .toList();

        DbSet.refreshmentsOrderDetails().insert(refreshments);

        // Print receipt
        RefreshmentReceiptPrinter.printReceipt(
                order.getId(),
                cart,
                refreshmentsList,
                calculateSubtotal()
        );

        showAlert("Success", "Order placed successfully!", Alert.AlertType.INFORMATION);
        onClearCart();
        changeScene.accept("user/refreshments-order-success.fxml", null);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
