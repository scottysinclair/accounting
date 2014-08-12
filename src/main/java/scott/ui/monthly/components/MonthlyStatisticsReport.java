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

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

public class MonthlyStatisticsReport extends Panel {
	private static final long serialVersionUID = 1L;

	private Table table;
	private BeanItemContainer<Statistic> beanContainer;

	public MonthlyStatisticsReport() {
		table = new Table("Monthly Report");
		table.setImmediate(true);
		table.setHeight("500px");
		beanContainer = new BeanItemContainer<Statistic>(Statistic.class, Collections.<Statistic>emptyList());
		table.setContainerDataSource(beanContainer);
		setContent(table);
	}

	public void regenerate(Month month, Collection<Transaction> transactions, Collection<Category> categories) {
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
	}

}
