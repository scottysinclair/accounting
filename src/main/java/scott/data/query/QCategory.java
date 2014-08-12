package scott.data.query;

import scott.data.model.Account;
import scott.data.model.Category;

import com.smartstream.sodit.api.query.QProperty;
import com.smartstream.sodit.api.query.QueryObject;

public class QCategory extends QueryObject<Category> {

	private static final long serialVersionUID = 1L;

	public QCategory() {
		super(Category.class);
	}

	public QCategory(QueryObject<?> parent) {
		super(Category.class, parent);
	}

	public QProperty<Long> id() {
		return new QProperty<Long>(this, "id");
	}
}
