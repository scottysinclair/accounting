package scott.ui.config.components;

import java.util.LinkedList;
import java.util.List;

import scott.data.model.Category;
import scott.data.query.QCategory;

import com.smartstream.sodit.api.core.entity.EntityContext;
import com.smartstream.sodit.server.jdbc.persister.PersistRequest;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

public abstract class Categories {

	private VerticalLayout vl;
	private Table table;
	private BeanItemContainer<Category> beanContainer;
	
	public Categories() {
		vl = new VerticalLayout();
		beanContainer = new BeanItemContainer<Category>(Category.class);
		
		table = new Table();
		table.setSelectable(true);
		table.setImmediate(true);
		table.setEditable(true);
		table.setContainerDataSource(beanContainer);
		table.setVisibleColumns("id", "name", "monthlyLimit");
		
		beanContainer.addAll( loadCategories(entityContext()) );
		
		
		vl.addComponent(table);
		Button add = new Button("Add");
		add.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Category cat = entityContext().newModel(Category.class);
				beanContainer.addBean(cat);
				table.setValue(cat);
				table.setCurrentPageFirstItemId(cat);
			}
		});
		HorizontalLayout hl = new HorizontalLayout();
		hl.addComponent(add);

		Button save = new Button("Save");
		save.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				PersistRequest request = new PersistRequest();
				for (Category cat: beanContainer.getItemIds()) {
					request.save(cat);
				}
				if (!request.getToSave().isEmpty()) {
					try {
						entityContext().persist(request);
					}
					catch(Exception x) {
						throw new IllegalStateException("oops", x);
					}
				}
			}
		});
		hl.addComponent(save);

		vl.addComponent(hl);
	}

	public List<Category> getCategories() {
		return new LinkedList<Category>(beanContainer.getItemIds());
	}
	public Component getComponent() {
		return vl;
	}
	
	protected abstract EntityContext entityContext();
	
	private List<Category> loadCategories(EntityContext entityContext) {
		try {
			QCategory qcat = new QCategory();
			qcat.orderBy(qcat.id(), true);
			return entityContext.performQuery(qcat).getList();
		} catch (Exception x) {
			throw new IllegalStateException("oops", x);
		}
	}
	
	
}
