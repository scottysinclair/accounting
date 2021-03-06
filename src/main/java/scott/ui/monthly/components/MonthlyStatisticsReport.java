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
	private boolean suppressCategoryChanged = false;

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
				if (suppressCategoryChanged) return;
				Statistic stat = (Statistic)event.getProperty().getValue();
				if (stat == null) {
					notifyCategoryChanged(null);
				}
				else {
					notifyCategoryChanged(getCategory(stat));
				}
			}
		});

		table.setVisibleColumns(new Object[] { "name", "count", "value"});
		table.setHeight("850px");

		setContent(table);
	}
	
	public void clearSelection() {
		table.setValue(null);
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
		suppressCategoryChanged = false;
		try {
			if (category == null) {
				return;
			}
			for (Statistic stat: beanContainer.getItemIds()) {
				if (stat.getName().equals(category.getName())) {
					table.setValue(stat);
					table.setCurrentPageFirstItemId(stat);
					return;
				}
			}
		}
		finally {
			suppressCategoryChanged = false;
		}
  }

	public void regenerate(Month month, Collection<Transaction> transactions, Collection<Category> categories) {
		Statistic selected = (Statistic)table.getValue();
		this.categories = categories;
		BigDecimal balance = month != null ? month.getStartingBalance() : null;
		BigDecimal totalIn = new BigDecimal("0");
		BigDecimal totalOut = new BigDecimal("0");
		
		Map<Category,BigDecimal> totalByCat = new HashMap<Category, BigDecimal>();
		Map<Category,Integer> countByCat = new HashMap<Category, Integer>();
		for (Category cat: categories) {
			totalByCat.put(cat, new BigDecimal("0"));
			countByCat.put(cat, new Integer(0));
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
			countByCat.put(t.getCategory(), countByCat.get( t.getCategory() )+1);
		}
		List<Statistic> statistics = new LinkedList<Statistic>();
		if (balance != null) {
			statistics.add(new Statistic("Start Balance", month.getStartingBalance(), null));
			statistics.add(new Statistic("End Balance", balance, null));
		}
		statistics.add(new Statistic("Total In", totalIn, null));
		statistics.add(new Statistic("Total Out", totalOut, null));
		for (Map.Entry<Category, BigDecimal> entry: totalByCat.entrySet()) {
			statistics.add(new Statistic(entry.getKey().getName(), entry.getValue(), countByCat.get(entry.getKey())));
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
