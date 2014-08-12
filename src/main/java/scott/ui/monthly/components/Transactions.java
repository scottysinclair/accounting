package scott.ui.monthly.components;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import scott.data.model.Account;
import scott.data.model.Category;
import scott.data.model.Month;
import scott.data.model.Transaction;
import scott.data.query.QMonth;
import scott.data.query.QTransaction;
import scott.ui.monthly.components.Transactions;
import scott.ui.monthly.model.MonthAndYear;

import com.smartstream.sodit.api.core.entity.EntityContext;
import com.smartstream.sodit.server.jdbc.queryexecution.QueryResult;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;

import static com.vaadin.data.util.BeanContainer.*;

import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;

public abstract class Transactions {
	VerticalLayout vl = new VerticalLayout();

	private Table table;
	private EditTransactionControls etc;
	private BeanItemContainer<Transaction> beanContainer;
	private Set<Transaction> deleted = new HashSet<Transaction>();

	public Transactions() {
		table = new Table("Transactions");
		table.addStyleName("transactions");
		table.setSelectable(true);
		table.setImmediate(true);


		beanContainer = new BeanItemContainer<Transaction>(Transaction.class);
		beanContainer.addItemSetChangeListener(new ItemSetChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				contentsChanged(beanContainer.getItemIds());
			}
		});
		table.setContainerDataSource(beanContainer);

		table.setConverter("category", new Converter<String,Category>(){
			private static final long serialVersionUID = 1L;

			@Override
			public Category convertToModel(String value, Class<? extends Category> targetType, Locale locale)
					throws ConversionException {
				return null;
			}

			@Override
			public String convertToPresentation(Category value, Class<? extends String> targetType, Locale locale)
					throws ConversionException {
				return value != null ? value.getName() : "";
			}

			@Override
			public Class<Category> getModelType() {
				return Category.class;
			}

			@Override
			public Class<String> getPresentationType() {
				return String.class;
			}
		});

		table.addGeneratedColumn("balance", new Table.ColumnGenerator() {
			private static final long serialVersionUID = 1L;
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				if (monthAndYear().getMonth() != null) {
					return getBalanceBeforeTransaction((Transaction)itemId);
				}
				else {
					return new BigDecimal("0");
				}
			}
		});


		table.setVisibleColumns(new Object[] { "balance", "date", "amount", "category", "comment", "important" });
		table.setTableFieldFactory(new MyTableFieldFactory(getCategories()));
		table.setEditable(true);

        monthAndYear().add(new MonthAndYear.Listener() {
			@Override
			public void changed(MonthAndYear monthAndYear) {
				load(monthAndYear().getStartOfMonth(), monthAndYear().getEndOfMonth());
				updateEditableState();
			}
		});


		load(monthAndYear().getStartOfMonth(), monthAndYear().getEndOfMonth());
		vl.addComponent(table);

		etc = new EditTransactionControls(){
			private static final long serialVersionUID = 1L;
			@Override
			protected BeanItemContainer<Transaction> beanContainer() {
				return beanContainer;
			}
			@Override
			protected Set<Transaction> deleted() {
				return deleted;
			}
			@Override
			protected Account account() {
				return Transactions.this.account();
			}
			@Override
			protected EntityContext entityContext() {
				return Transactions.this.entityContext();
			}
			@Override
			protected Table table() {
				return table;
			}
			@Override
			protected void onBeforeSave() {
				Transactions.this.onBeforeSave();
			}
			@Override
			protected void reloadTable() {
				Transaction selectedTransaction = (Transaction)table.getValue();
				load(Transactions.this.monthAndYear().getStartOfMonth(), Transactions.this.monthAndYear().getEndOfMonth());
				if (selectedTransaction != null) {
					table.setValue( selectedTransaction );
				}
			}
			@Override
			protected boolean isEditingOpen() {
				return isEditable();
			}
			@Override
			protected void openEditing() {
				openMonth();
				updateEditableState();
			}
			@Override
			protected void finishEditing() {
				finishMonth(getFinalBalance());
				Transactions.this.updateEditableState();
			}
		};
		vl.addComponent(etc);
		updateEditableState();
	}

	private BigDecimal getFinalBalance() {
		List<Transaction> transactions = beanContainer.getItemIds();
		Transaction finalT = transactions.get( transactions.size() - 1);
		BigDecimal balance = getBalanceBeforeTransaction(finalT);
		return balance.add( finalT.getAmount() );
	}

	private BigDecimal getBalanceBeforeTransaction(Transaction transaction) {
		List<Transaction> transactions = beanContainer.getItemIds();
		int index = transactions.indexOf(transaction);
		BigDecimal balance = monthAndYear().getMonth().getStartingBalance();
		for (Transaction t: transactions.subList(0, index)) {
			if (t.getAmount() == null) {
				return null;
			}
			balance = balance.add( t.getAmount() );
		}
		return balance;
	}

	/**
	 * We are editable if the current month is not finished and has a starting balance
	 * @return
	 */
	private boolean isEditable() {
		Month month = monthAndYear().getMonth();
		return month != null && month.getStartingBalance() != null && !month.getFinished();
	}

	private boolean isMonthFinished() {
		Month month = monthAndYear().getMonth();
		return month != null && month.getStartingBalance() != null && month.getFinished();
	}

	private void updateEditableState() {
		boolean editable = isEditable();
		if (editable){
			table.setEditable( true );
			etc.setEditable();
		}
		else if (isMonthFinished()) {
			table.setEditable( false );
			etc.setFinished();
		}
		else {
			table.setEditable( false );
			etc.setDisabled();
		}
	}

	public Component getComponent() {
		return vl;
	}

	protected abstract List<Category> getCategories();

	protected abstract EntityContext entityContext();

	protected abstract MonthAndYear monthAndYear();

	protected abstract Account account();

	protected abstract void openMonth();

	protected abstract void finishMonth(BigDecimal endBalance);

	protected void onBeforeSave() {}

	protected void contentsChanged(Collection<Transaction> transactions) {}


	public void load(Date from, Date to) {
		QTransaction qtrans = new QTransaction();
		qtrans.joinToAccount();
		qtrans.joinToCategory();

		qtrans.where(  qtrans.accountId().equal( account().getId() )
				.and(  qtrans.date().greaterOrEqual(from) )
				.and(  qtrans.date().lessOrEqual(to)  ))
				.orderBy(qtrans.date(), true);

		try {
			entityContext().removeAll(Transaction.class);
			beanContainer.removeAllItems();

			QueryResult<Transaction> result = entityContext().performQuery(qtrans);

			beanContainer.addAll(result.getList());
		} catch (Exception x) {
			throw new RuntimeException("oops", x);
		}
	}


	private class MyTableFieldFactory extends DefaultFieldFactory {
		private static final long serialVersionUID = 1L;
		private final BeanItemContainer<Category> categoriesContainer;
		public MyTableFieldFactory(List<Category> categories) {
			categoriesContainer = new BeanItemContainer<Category>(Category.class);
	    	for (Category cat: categories) {
	    		categoriesContainer.addItem(cat);
	    	}
		}

		@Override
		public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
			if ("category".equals(propertyId)) {
				/*
				 * The returned combobox will be automatically bound
				 * to the 'category' transaction property
				 */
				Transaction transaction = ((BeanItem<Transaction>)container.getItem(itemId)).getBean();
				return newCategoriesComboBox(transaction);
			}
			else if ("date".equals(propertyId)) {
				final ObjectProperty<Date> property = new ObjectProperty<Date>(monthAndYear().toDate());
				TextField dateField = new TextField(property);
				dateField.setConverter(new Converter<String,Date>(){
					private static final long serialVersionUID = 1L;
					@Override
					public Date convertToModel(String value, Class<? extends Date> targetType, Locale locale)
							throws ConversionException {
						if (value == null) {
							return null;
						}
						Calendar cal = monthAndYear().toCalendar();
						cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value));
						cal.set(Calendar.HOUR_OF_DAY, 10);
						cal.set(Calendar.MINUTE, 0);
						return cal.getTime();
					}

					@Override
					public String convertToPresentation(Date value, Class<? extends String> targetType, Locale locale)
							throws ConversionException {
						if (value == null) {
							return null;
						}
						Calendar cal = new GregorianCalendar();
						cal.setTime(value);
						return toString(cal.get(Calendar.DAY_OF_MONTH));
					}
					@Override
					public Class<Date> getModelType() {
						return Date.class;
					}
					@Override
					public Class<String> getPresentationType() {
						return String.class;
					}
					private String toString(int value) {
						return value < 10 ? "0" + value : String.valueOf(value);
					}
				});
				dateField.setNullRepresentation("");
				return dateField;
				/*
				DateField dateField = (DateField)super.createField(container, itemId, propertyId,
						uiContext);
				dateField.setResolution(Resolution.MINUTE);
				dateField.setDateFormat("")
				return dateField;
				*/
			}
			else if ("comment".equals(propertyId) || "amount".equals(propertyId)) {
				TextField tf = (TextField)super.createField(container, itemId, propertyId, uiContext);
				tf.setNullRepresentation("");
				return tf;
			}
			else if ("important".equals(propertyId)) {
				CheckBox checkBox = (CheckBox)super.createField(container, itemId, propertyId, uiContext);
				checkBox.setCaption("");
				return checkBox;
			}
			return super.createField(container, itemId, propertyId, uiContext);
		}

		private ComboBox newCategoriesComboBox(Transaction transaction) {
			ComboBox select = new FixedComboBox("Categories", categoriesContainer);
			select.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		    select.setItemCaptionPropertyId("name");
		    select.setImmediate(true);
		    select.setNullSelectionAllowed(false);
		    select.setValue( transaction.getCategory() );
		    return select;
		}

	}

}

class FixedComboBox extends ComboBox {       //      http://dev.vaadin.com/ticket/10544
		private static final long serialVersionUID = 1L;
		private boolean inFilterMode;

        public FixedComboBox(String caption, Container dataSource) {
			super(caption, dataSource);
			// TODO Auto-generated constructor stub
		}

		@Override
        public void containerItemSetChange (com.vaadin.data.Container.ItemSetChangeEvent event)
        {
                if (inFilterMode) {
                        super.containerItemSetChange(event);
                }
        }

        @Override
        protected List<?> getOptionsWithFilter (boolean needNullSelectOption)
        {
                try {
                        inFilterMode = true;
                        return super.getOptionsWithFilter(needNullSelectOption);
                }
                finally {
                        inFilterMode = false;
                }
        }
}