package scott.ui.monthly.components;

import scott.ui.monthly.model.MonthAndYear;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;

public class MonthAndYearChooser extends HorizontalLayout {
	private static final long serialVersionUID = 1L;
	private final MonthAndYear monthAndYear;

	public MonthAndYearChooser(MonthAndYear monthAndYear) {
		this.monthAndYear = monthAndYear;

        Button back = new Button("Prev");
        back.addStyleName("monthButton");
        back.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				MonthAndYearChooser.this.monthAndYear.prevMonth();
			}
		});
		addComponent(back);

		final Label label = new Label(monthAndYear.getText());
		label.addStyleName("monthLabel");
		addComponent(label);

        Button forward = new Button("Next");
        forward.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				MonthAndYearChooser.this.monthAndYear.nextMonth();
			}
		});
        forward.addStyleName("monthButton");
        addComponent(forward);
        addStyleName("monthControls");

        monthAndYear.add(new MonthAndYear.Listener() {
			@Override
			public void changed(MonthAndYear monthAndYear) {
				label.setValue(monthAndYear.getText());
			}
		});
	}
}
