package scott.ui.monthly.components;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import scott.data.model.Category;
import scott.data.model.Month;
import scott.data.model.Transaction;
import scott.ui.monthly.model.Statistic;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

public abstract class MonthlyStatisticsReport extends Panel {
	private static final long serialVersionUID = 1L;

	private Table table;
	private BeanItemContainer<Statistic> beanContainer;
	private Collection<Category> categories;

	public MonthlyStatisticsReport() {
		table = new Table("Monthly Report");
		table.setImmediate(true);
		table.setHeight("500px");
		table.setSelectable(true);
		beanContainer = new BeanItemContainer<Statistic>(Statistic.class, Collections.<Statistic>emptyList());
		table.setContainerDataSource(beanContainer);
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Statistic stat = (Statistic)event.getProperty().getValue();
				if (stat == null) {
					notifyCategoryChanged(null);
				}
				else {
					notifyCategoryChanged(getCategory(stat));
				}
			}
		});
		setContent(table);
	}
	
	protected abstract void notifyCategoryChanged(Category category);
	
	public Category getSelectedCategory() {
		Statistic stat = (Statistic)table.getValue();
		if (stat == null) {
			return null;
		}
		return getCategory(stat);
	}
	
	private Category getCategory(Statistic stat) {
		for (Category cat: categories) {
			if (cat.getName().equals( stat.getName() )) {
				return cat;
			}
		}
		return null;
	}
	
	public void setSelectedCategory(Category category) {
		for (Statistic stat: beanContainer.getItemIds()) {
			if (stat.getName().equals(category.getName())) {
				table.setValue(stat);
				table.setCurrentPageFirstItemId(stat);
				return;
			}
		}
	}

	public void regenerate(Month month, Collection<Transaction> transactions, Collection<Category> categories) {
		Statistic selected = (Statistic)table.getValue();
		this.categories = categories;
		BigDecimal balance = month != null ? month.getStartingBalance() : null;
		BigDecimal totalIn = new BigDecimal("0");
		BigDecimal totalOut = new BigDecimal("0");
		
		Map<Category,BigDecimal> totalByCat = new HashMap<Category, BigDecimal>();
		for (Category cat: categories) {
			totalByCat.put(cat, new BigDecimal("0"));
		}
		
		for (Transaction t: transactions) {
			if (t.getAmount() == null) {
				continue;
			}
			if (t.getAmount().doubleValue() > 0) {
				totalIn = totalIn.add(t.getAmount());
			}
			else {
				totalOut = totalOut.add(t.getAmount());
			}
			if (balance != null) {
				balance = balance.add( t.getAmount() );
			}
			totalByCat.put(t.getCategory(), totalByCat.get( t.getCategory() ).add( t.getAmount() ));
		}
		List<Statistic> statistics = new LinkedList<Statistic>();
		if (balance != null) {
			statistics.add(new Statistic("Start Balance", month.getStartingBalance().toString()));
			statistics.add(new Statistic("End Balance", balance.toString()));
		}
		statistics.add(new Statistic("Total In", totalIn.toString()));
		statistics.add(new Statistic("Total Out", totalOut.toString()));
		for (Map.Entry<Category, BigDecimal> entry: totalByCat.entrySet()) {
			statistics.add(new Statistic(entry.getKey().getName(), entry.getValue().toString()));
		}
		beanContainer.removeAllItems();
		beanContainer.addAll(statistics);
		if (selected != null) {
			for (Statistic stat: statistics) {
				if (stat.getName().equals(selected.getName())) {
					table.setValue(stat);
				}
			}
		}
	}

}
