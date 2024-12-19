/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import es.deusto.sd.strava.client.data.Article;
import es.deusto.sd.strava.client.data.Category;

/**
 * SwingClientGUI class is a Swing-based client that demonstrates the usage of the
 * AuctionsService. It is implemented using a classic Controller pattern.
 */
public class SwingClientGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	// Controller instance
	private final SwingClientController controller;

	// Default login credentials
	private String defaultEmail = "blackwidow@marvel.com";
	private String defaultPassword = "Bl@ckWid0w2023";

	private JLabel logoutLabel;
	private JComboBox<String> currencyComboBox;
	private JList<Category> categoryList;
	private JTable jtbleArticles;
	private JLabel lblArticleTitle;
	private JLabel lblArticlePrice;
	private JLabel lblArticleBids;
	private JSpinner spinBidAmount;
	private JButton btnBid;

	private static final String[] CURRENCIES = { "EUR", "USD", "GBP", "JPY" };

	public SwingClientGUI(SwingClientController controller) {
		this.controller = controller;

		if (!performLogin()) {
			System.exit(0);
		}

		setTitle("Auctions Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 400);
		setResizable(false);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());

		// Currency ComboBox
		currencyComboBox = new JComboBox<>(CURRENCIES);
		currencyComboBox.setSelectedItem("EUR"); // Default currency
		currencyComboBox.addActionListener(e -> {
			loadArticleDetails();
			loadArticlesForCategory();
		});
		topPanel.add(currencyComboBox, BorderLayout.WEST);

		// Logout Label
		logoutLabel = new JLabel("Logout", SwingConstants.RIGHT);
		logoutLabel.setForeground(Color.BLUE);
		logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		logoutLabel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				performLogout();
			}
		});
		topPanel.add(logoutLabel, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		// Category List
		categoryList = new JList<>();
		categoryList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					loadArticlesForCategory();
				}
			}
		});
		
		categoryList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
			JLabel label = new JLabel(value.name());
			label.setBackground(list.getBackground());
			label.setOpaque(true);
		
			if (isSelected) {
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
			}
			
			return label;
		});
		
		JScrollPane categoryScrollPane = new JScrollPane(categoryList);
		categoryScrollPane.setPreferredSize(new Dimension(200, getHeight()));
		categoryScrollPane.setBorder(new TitledBorder("Categories"));
		add(categoryScrollPane, BorderLayout.WEST);

		// Articles Table
		jtbleArticles = new JTable(new DefaultTableModel(new Object[] { "ID", "Title", "Current Price", "Bids" }, 0)) {
			private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
		};
		jtbleArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtbleArticles.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				loadArticleDetails();
			}
		});
		jtbleArticles.getColumnModel().getColumn(0).setMaxWidth(40);
		jtbleArticles.getColumnModel().getColumn(1).setPreferredWidth(200);
		jtbleArticles.getColumnModel().getColumn(3).setMaxWidth(40);

		JScrollPane articleScrollPane = new JScrollPane(jtbleArticles);
		articleScrollPane.setPreferredSize(new Dimension(600, getHeight()));
		articleScrollPane.setBorder(new TitledBorder("Articles of the selected Category"));
		add(articleScrollPane, BorderLayout.CENTER);

		// Article Details
		JPanel jPanelArticleDetails = new JPanel(new GridLayout(5, 2, 10, 10));
		jPanelArticleDetails.setBorder(new TitledBorder("Article Details"));
		jPanelArticleDetails.setPreferredSize(new Dimension(300, getHeight())); // Remaining width

		jPanelArticleDetails.add(new JLabel("Title:"));
		lblArticleTitle = new JLabel();
		jPanelArticleDetails.add(lblArticleTitle);

		jPanelArticleDetails.add(new JLabel("Current Price:"));
		lblArticlePrice = new JLabel();
		jPanelArticleDetails.add(lblArticlePrice);

		jPanelArticleDetails.add(new JLabel("Bids:"));
		lblArticleBids = new JLabel();
		jPanelArticleDetails.add(lblArticleBids);

		jPanelArticleDetails.add(new JLabel("Bid Amount:"));
		spinBidAmount = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		jPanelArticleDetails.add(spinBidAmount);

		btnBid = new JButton("Place Bid");
		btnBid.setEnabled(false);
		btnBid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				placeBid();
			}
		});

		JPanel jPanelBidButton = new JPanel();
		jPanelBidButton.add(btnBid);
		jPanelArticleDetails.add(jPanelBidButton);

		add(jPanelArticleDetails, BorderLayout.EAST);

		loadCategories();
		setVisible(true);
	}

	private boolean performLogin() {
		JTextField emailField = new JTextField(20);
		emailField.setText(defaultEmail);
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setText(defaultPassword);

		Object[] message = { new JLabel("Enter Email:"), emailField, new JLabel("Enter Password:"), passwordField };

		int option = JOptionPane.showConfirmDialog(this, message, "Login", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION) {
			try {
				return controller.login(emailField.getText(), new String(passwordField.getPassword()));
			} catch (RuntimeException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	private void performLogout() {
		try {
			controller.logout();
			JOptionPane.showMessageDialog(this, "Logged out successfully.");
			System.exit(0);
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private void loadCategories() {
		try {
			List<Category> categories = controller.getCategories();

			SwingUtilities.invokeLater(() -> {
				categoryList.setListData(categories.toArray(new Category[0]));
			});
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private void loadArticlesForCategory() {
		Category selectedCategory = categoryList.getSelectedValue();
		String currency = (String) currencyComboBox.getSelectedItem();

		if (selectedCategory != null) {
			try {
				List<Article> articles = controller.getArticlesByCategory(selectedCategory.name(), currency);

				SwingUtilities.invokeLater(() -> {
					DefaultTableModel model = (DefaultTableModel) jtbleArticles.getModel();
					model.setRowCount(0);

					for (Article article : articles) {
						model.addRow(new Object[] { article.id(), article.title(),
								formatPrice(article.currentPrice(), currency), article.bids() });
					}
				});
			} catch (RuntimeException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	private void loadArticleDetails() {
		int selectedRow = jtbleArticles.getSelectedRow();
		String currency = (String) currencyComboBox.getSelectedItem();

		if (selectedRow != -1) {
			Long articleId = (Long) jtbleArticles.getValueAt(selectedRow, 0);

			try {
				Article article = controller.getArticleDetails(articleId, currency);

				SwingUtilities.invokeLater(() -> {
					lblArticleTitle.setText(article.title());
					lblArticlePrice.setText(formatPrice(article.currentPrice(), currency));
					lblArticleBids.setText(String.valueOf(article.bids()));
					spinBidAmount.setValue((int) Math.ceil(article.currentPrice()) + 1);
					btnBid.setEnabled(true);
				});
			} catch (RuntimeException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	private void placeBid() {
		int selectedRow = jtbleArticles.getSelectedRow();
		String currency = (String) currencyComboBox.getSelectedItem();

		if (selectedRow != -1) {
			Long articleId = (Long) jtbleArticles.getValueAt(selectedRow, 0);
			Float bidAmount = ((Integer) spinBidAmount.getValue()).floatValue();

			try {
				controller.placeBid(articleId, bidAmount, currency);

				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(this, "Bid placed successfully!");
				});

				loadArticleDetails();
				loadArticlesForCategory();

				SwingUtilities.invokeLater(() -> {
					jtbleArticles.setRowSelectionInterval(selectedRow, selectedRow);
				});
			} catch (RuntimeException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	private String formatPrice(float price, String currency) {
		return switch (currency) {
				case "USD" -> String.format("$ %.2f", price);
				case "GBP" -> String.format("%.2f £", price);
				case "JPY" -> String.format("¥ %.2f", price);
				default -> String.format("%.2f €", price);
		};
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SwingClientGUI(new SwingClientController()));
	}
}