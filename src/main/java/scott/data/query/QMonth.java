package scott.data.query;

import java.util.Date;

import scott.data.model.Month;

import com.smartstream.sodit.api.query.QProperty;
import com.smartstream.sodit.api.query.QueryObject;

public class QMonth extends QueryObject<Month> {

	private static final long serialVersionUID = 1L;

	public QMonth() {
		super(Month.class);
	}

	public QMonth(QueryObject<?> parent) {
		super(Month.class, parent);
	}

	public QProperty<Date> starting() {
		return new QProperty<Date>(this, "starting");
	}

}
