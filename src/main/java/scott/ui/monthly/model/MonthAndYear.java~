package scott.ui.monthly.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import scott.data.model.Month;

public abstract class MonthAndYear {

	public interface Listener {
		public void changed(MonthAndYear monthAndYear);
	}

	private static String months[] = {
			"January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December"
			};

	private List<Listener> listeners = new LinkedList<Listener>();

	private final Calendar cal;
	private final Calendar calTmp;

	public MonthAndYear() {
		cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		calTmp = new GregorianCalendar();
	}

	public abstract Month getMonth();

	public abstract Month getPreviousMonth();

	public abstract Month getNextMonth();

	public Calendar toCalendar() {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(cal.getTimeInMillis());
		return c;
	}

	public Date toDate() {
		return cal.getTime();
	}

	public String getText() {
		final int monthOfYear = cal.get(Calendar.MONTH);
		return String.valueOf(cal.get(Calendar.YEAR)) + " " + months[ monthOfYear ];
	}

	public Date getStartOfMonth() {
		calTmp.setTimeInMillis(cal.getTimeInMillis());
		calTmp.set(Calendar.DAY_OF_MONTH, 1);
		return calTmp.getTime();
	}

	public Date getStartOfPreviousMonth() {
		calTmp.setTimeInMillis(cal.getTimeInMillis());
		calTmp.add(Calendar.MONTH, -1);
		calTmp.set(Calendar.DAY_OF_MONTH, 1);
		return calTmp.getTime();
	}

	public Date getStartOfNextMonth() {
		calTmp.setTimeInMillis(cal.getTimeInMillis());
		calTmp.add(Calendar.MONTH, +1);
		calTmp.set(Calendar.DAY_OF_MONTH, 1);
		return calTmp.getTime();
	}

	public Date getEndOfMonth() {
		calTmp.setTimeInMillis(cal.getTimeInMillis());
		calTmp.set(Calendar.DAY_OF_MONTH, 1);
		calTmp.add(Calendar.MONTH, 1);
		calTmp.add(Calendar.DAY_OF_YEAR, -1);
		return calTmp.getTime();
	}

	public void nextMonth() {
		cal.add(Calendar.MONTH, 1);
		notifyMonthChanged();
	}

	public void prevMonth() {
		cal.add(Calendar.MONTH, -1);
		notifyMonthChanged();
	}

	public void add(Listener listener) {
		listeners.add(listener);
	}

	protected void notifyMonthChanged() {
		for (Listener listener: listeners) {
			listener.changed(this);
		}
	}

}
