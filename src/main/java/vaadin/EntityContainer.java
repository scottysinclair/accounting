package vaadin;

import java.util.Collection;

import com.smartstream.sodit.api.config.EntityType;
import com.smartstream.sodit.api.core.entity.EntityContext;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class EntityContainer implements Container {

	private EntityContext entityContext;
	private EntityType entityType;

	@Override
	public Item getItem(Object itemId) {
		return null;
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return null;
	}

	@Override
	public Collection<?> getItemIds() {
		return null;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		return null;
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean containsId(Object itemId) {
		return false;
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		return null;
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		return null;
	}

	@Override
	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		return false;
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
		return false;
	}

	@Override
	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		return false;
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		return false;
	}

}
