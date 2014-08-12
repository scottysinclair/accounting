package scott.ui.monthly.components;

import java.util.List;
import java.util.Set;

import scott.data.model.Account;
import scott.data.model.Transaction;

import com.smartstream.sodit.api.core.entity.EntityContext;
import com.smartstream.sodit.server.jdbc.persister.PersistRequest;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;

public abstract class EditTransactionControls extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	private Button remove;
	private Button add;
	private Button save;
	private Button finishReopen;

	protected abstract BeanItemContainer<Transaction> beanContainer();

	protected abstract Set<Transaction> deleted();

	protected abstract Account account();

	protected abstract EntityContext entityContext();

	protected abstract Table table();

	protected abstract void onBeforeSave();

	protected abstract void reloadTable();

	protected abstract boolean isEditingOpen();

	protected abstract void openEditing();

	protected abstract void finishEditing();

	public EditTransactionControls() {
		save = new Button("Save");
		save.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				doSave();
			}
		});
		save.addStyleName("transactionControlButton");
		add = new Button("Add");
		add.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
			  Transaction transaction = entityContext().newModel(Transaction.class);
			  transaction.setImportant(false);
			  transaction.setAccount(account());
			  List<Transaction> transactions = beanContainer().getItemIds();
			  if (!transactions.isEmpty()) {
				  transaction.setDate(transactions.get( transactions.size()-1 ).getDate());
			  }
			  beanContainer().addBean(transaction);
			  table().setValue(transaction);
			  table().setCurrentPageFirstItemId(transaction);
			}
		});
		add.addStyleName("transactionControlButton");

		remove = new Button("Remove");
		remove.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				Transaction transaction = (Transaction)table().getValue();
				List<Transaction> transactions = beanContainer().getItemIds();
				int currentSelectedIndex = transactions.indexOf(transaction);
				Transaction nextSelection = null;
				if (currentSelectedIndex < (transactions.size() - 1)) {
					nextSelection = transactions.get( currentSelectedIndex+1 );
				}
				else if (transactions.size() > 1) {
					nextSelection = transactions.get( currentSelectedIndex-1 );
				}

				if (transaction != null) {
					beanContainer().removeItem(transaction);
					if (transaction.getId() != null) {
						deleted().add(transaction);
					}
					if (nextSelection != null) {
						table().setValue(nextSelection);
					}
				}
			}
		});
		remove.addStyleName("transactionControlButton");

		finishReopen = new Button(isEditingOpen() ? "Finish" : "Open");
		finishReopen.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				if (isEditingOpen()) {
					doSave();
					finishEditing();
				}
				else {
					openEditing();
				}
			}
		});
		finishReopen.addStyleName("transactionControlButton");

		addComponent(remove);
		addComponent(add);
		addComponent(save);
		addComponent(finishReopen);
	}
	
	private void doSave() {
		onBeforeSave();
		PersistRequest request = new PersistRequest();
		for (Transaction transaction: beanContainer().getItemIds()) {
			request.save(transaction);
			System.out.println("Added save " + transaction.getDate());
		}
		for (Transaction transaction: deleted()) {
			request.delete(transaction);
			System.out.println("Added delete " + transaction.getDate());
		}
		try {
			if (!request.getToSave().isEmpty() || !request.getToDelete().isEmpty()) {
				entityContext().persist(request);
				deleted().clear();
				reloadTable();
			}
		}
		catch(Exception x) {
			throw new RuntimeException("Could not save transactions", x);
		}
	}

	public void setEditable() {
		remove.setEnabled(true);
		add.setEnabled(true);
		save.setEnabled(true);
		finishReopen.setCaption("Finish");
		if (beanContainer().size() > 0) {
			finishReopen.setEnabled(true);
		}
	}
	
	public void setFinished() {
		remove.setEnabled(false);
		add.setEnabled(false);
		save.setEnabled(false);
		finishReopen.setEnabled(true);
		finishReopen.setCaption("Reopen");
	}

	public void setDisabled() {
		remove.setEnabled(false);
		add.setEnabled(false);
		save.setEnabled(false);
		finishReopen.setEnabled(false);
		finishReopen.setCaption("Reopen");
	}

}
