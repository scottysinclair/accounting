package scott;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import scott.data.AccountingDefinitions;
import scott.data.model.Account;
import scott.data.model.Category;
import scott.data.model.Month;
import scott.data.model.Transaction;
import scott.data.query.QAccount;
import scott.data.query.QCategory;
import scott.data.query.QMonth;
import scott.ui.monthly.components.MonthAndYearChooser;
import scott.ui.monthly.components.MonthlyStatisticsReport;
import scott.ui.monthly.components.Transactions;
import scott.ui.monthly.model.MonthAndYear;

import com.smartstream.sodit.api.core.entity.DeleteListener;
import com.smartstream.sodit.api.core.entity.EntityContext;
import com.smartstream.sodit.server.jdbc.persister.PersistRequest;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

@Theme("mytheme")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "scott.AppWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	private MonthAndYear monthAndYear;
	private Transactions transactions;

	@Override
    protected void init(VaadinRequest request) {

    	final EntityContext entityContext = AccountingDefinitions.newContext();

    	class MyMonthAndYear extends MonthAndYear implements DeleteListener<Object> {
    		private Month month;
    		private Month previousMonth;
    		private Month nextMonth;
    		private Map<Date,Month> cache = new HashMap<Date, Month>();
    		private boolean triedToLoad;
			@Override
			public Month getMonth() {
				lazyLoad();
				return month;
			}
			@Override
			public Month getPreviousMonth() {
				lazyLoad();
				return previousMonth;
			}
			@Override
			public Month getNextMonth() {
				lazyLoad();
				return nextMonth;
			}
			
			@Override
			public void entityDeleted(Object month) {
				if (this.month == month) {
					this.month = null;
				}
				if (this.previousMonth == month) {
					this.previousMonth = null;
				}
				if (this.nextMonth == month) {
					this.nextMonth = null;
				}
				for (Iterator<Map.Entry<Date, Month>> i = cache.entrySet().iterator(); i.hasNext();) {
					Map.Entry<Date, Month> e = i.next();
					if (e.getValue() == month) {
						i.remove();
					}
				}
				System.out.println("cleared months");
			}

			private void lazyLoad() {
				if (!triedToLoad ) {
					previousMonth = cache.get(getStartOfPreviousMonth());
					month = cache.get(getStartOfMonth());
					nextMonth = cache.get(getStartOfNextMonth());
					
					if (previousMonth == null || month == null || nextMonth == null) {
						MonthData monthData = getCurrentMonthData(entityContext, this, previousMonth, month, nextMonth);
						if (monthData != null) {
							if (monthData.current != null) {
								month = monthData.current;
								cache.put(month.getStarting(), month);
								entityContext.addDeleteListener(monthData.current, this);
							}
							if (monthData.prev != null) {
								previousMonth = monthData.prev;
								cache.put(previousMonth.getStarting(), previousMonth);
								entityContext.addDeleteListener(monthData.prev, this);
							}
							if (monthData.next != null) {
								nextMonth = monthData.next;
								cache.put(nextMonth.getStarting(), nextMonth);
								entityContext.addDeleteListener(monthData.next, this);
							}
						}
					}
					triedToLoad = true;
				}
			}

			@Override
			protected void notifyMonthChanged() {
				triedToLoad = false;
				super.notifyMonthChanged();
			}
    	}

    	monthAndYear = new MyMonthAndYear();

    	Panel rootPanel  = new Panel();
    	rootPanel.addStyleName("rootContainer");
    	rootPanel.setSizeUndefined();
    	VerticalLayout vl = new VerticalLayout();

		MonthAndYearChooser monthAndYearChooser = new MonthAndYearChooser(monthAndYear);
		vl.addComponent(monthAndYearChooser);

		final Account account = loadOrCreateAccount(entityContext);


    	HorizontalLayout hl = new HorizontalLayout();

        final Label errors = new Label("", ContentMode.HTML);

        final List<Category> categories = loadCategories(entityContext);


        final MonthlyStatisticsReport report = new MonthlyStatisticsReport();
        transactions = new Transactions(){
			@Override
			protected void onBeforeSave() {
				errors.setValue("");
			}
			@Override
			protected void contentsChanged(Collection<Transaction> transactions) {
				report.regenerate(monthAndYear.getMonth(), transactions, categories);
			}

			@Override
			protected List<Category> getCategories() {
				return categories;
			}
			@Override
			protected EntityContext entityContext() {
				return entityContext;
			}
			@Override
			protected MonthAndYear monthAndYear() {
				return monthAndYear;
			}
			@Override
			protected Account account() {
				return account;
			}
			@Override
			protected void openMonth() {
				openMonthAndDeleteFollowingMonths(entityContext, monthAndYear.getMonth());
			}
			@Override
			protected void finishMonth(BigDecimal endBalance) {
				monthAndYear.getMonth().setFinished(true);
				Month nextMonth = getOrCreateNextMonth(entityContext);
				nextMonth.setStarting(monthAndYear.getStartOfNextMonth());
				nextMonth.setStartingBalance(endBalance);
				nextMonth.setFinished(false);
				saveMonths(entityContext, monthAndYear.getMonth(), nextMonth);
			}
        };
        
        hl.addComponent(transactions.getComponent());
        hl.addComponent(report);

        vl.addComponent(hl);
        vl.addComponent(errors);

        rootPanel.setContent(vl);
        setContent(rootPanel);

     // Configure the error handler for the UI
        UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                // Find the final cause
                String cause = "<b>Error:</b><br/>";
                for (Throwable t = event.getThrowable(); t != null;
                     t = t.getCause())
                    if (t.getCause() == null) // We're at final cause
                        cause += t.getClass().getName() + "<br/>";

                errors.setValue(cause);

                event.getThrowable().printStackTrace(System.err);

            }
        });
    }

	private Month getOrCreateNextMonth(EntityContext entityContext) {
		Month month = monthAndYear.getNextMonth();
		if (month == null) {
			month = entityContext.newModel(Month.class);
		}
		return month;
	}

	private void openMonthAndDeleteFollowingMonths(EntityContext entityContext, Month month) {
		month.setFinished(false);
		QMonth qmonth = new QMonth();
		qmonth.where(qmonth.starting().greater(month.getStarting()));

		try {
			PersistRequest persistRequest = new PersistRequest();
			for (Month toDelete: entityContext.performQuery(qmonth).getList()) {
				persistRequest.delete(toDelete);
			}
			if (!persistRequest.getToDelete().isEmpty()) {
				entityContext.persist(persistRequest);
			}
		}
		catch(Exception x) {
			throw new IllegalStateException("oops", x);
		}
	}

	private void saveMonths(EntityContext entityContext, Month ...months) {
		try {
			PersistRequest request = new PersistRequest();
			for (Month m: months) {
				request.save(m);
			}
			if (!request.getToSave().isEmpty()) {
				entityContext.persist(request);
			}
		}
		catch(Exception x) {
			throw new IllegalStateException("oops", x);
		}
	}

	class MonthData {
		public final Month prev;
		public final Month current;
		public final Month next;
		public MonthData(Month prev, Month current, Month next) {
			this.prev = prev;
			this.current = current;
			this.next = next;
		}
	}

	/**
	 * Gets the previous month and the current month and the next month
	 * @param entityContext
	 * @param monthAndYear
	 * @return
	 */
	private MonthData getCurrentMonthData(EntityContext entityContext, MonthAndYear monthAndYear, Month p, Month c, Month n) {
        try {
        	/*
        	 * todo
        	 * load into another entitycontext
        	 * and then copy into  if they don't already exist
        	 */
	        QMonth qmonth = new QMonth();
	        if (c == null) {
		        qmonth.where(  qmonth.starting().equal(monthAndYear.getStartOfMonth()) );
	        }
	        if (p == null) {
	        	qmonth.or(     qmonth.starting().equal(monthAndYear.getStartOfPreviousMonth())  );
	        }
	        if (n == null) {
		        qmonth.or(     qmonth.starting().equal(monthAndYear.getStartOfNextMonth())  );
	        }
	        qmonth.orderBy(qmonth.starting(), true);
	        List<Month> months = entityContext.performQuery(qmonth).getList();
	        if (months.isEmpty()) {
	        	return null;
	        }
	        if (months.size() == 3) {
	        	return new MonthData(months.get(0), months.get(1), months.get(2));
	        }
	        else if (months.size() < 3) {
	        	MonthData monthData = new MonthData(
	        			match(monthAndYear.getStartOfPreviousMonth(), months),
	        			match(monthAndYear.getStartOfMonth(), months),
	        			match(monthAndYear.getStartOfNextMonth(), months));

	        	if (monthData.prev == null && monthData.current == null && monthData.next == null) {
	        		throw new IllegalStateException("Invalid month returned for " + monthAndYear.getText() + "  " + months.get(0));
	        	}
	        	return monthData;
	        }
	        else {
	        	throw new IllegalStateException("Too many months returned for " + monthAndYear.getText() + "  " + months.size());
	        }
        }
        catch(Exception x) {
          throw new IllegalStateException("oops", x);
        }
	}

	private Month match(Date date, List<Month> months) {
		for (Month m: months) {
			if (m.getStarting().equals(date)) {
				return m;
			}
		}
		return null;
	}

	private List<Category> loadCategories(EntityContext entityContext) {
		try {
			return entityContext.performQuery(new QCategory()).getList();
		} catch (Exception x) {
			throw new IllegalStateException("oops", x);
		}
	}

	private Account loadOrCreateAccount(EntityContext entityContext) {
        QAccount qaccount = new QAccount();
        qaccount.where(qaccount.name().equal("Bank Austria"));
        Account account;
        try {
        	return account = entityContext.performQuery(qaccount).getList().get(0);
        }
        catch(Exception x) {
        	account = entityContext.newModel(Account.class);
        	account.setName("Bank Austria");
        	try {
        		entityContext.persist(new PersistRequest().save(account));
        		return account;
        	}
        	catch(Exception x2) {
        		x.addSuppressed(x2);
        		throw new IllegalStateException("oops", x);
        	}
        }

	}
}