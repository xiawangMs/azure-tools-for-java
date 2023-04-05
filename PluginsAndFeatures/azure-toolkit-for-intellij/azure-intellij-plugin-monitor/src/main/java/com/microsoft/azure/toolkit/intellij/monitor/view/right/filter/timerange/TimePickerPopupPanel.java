package com.microsoft.azure.toolkit.intellij.monitor.view.right.filter.timerange;

import com.intellij.ui.JBIntSpinner;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.michaelbaranov.microba.calendar.CalendarPane;
import lombok.Getter;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Calendar;
import java.util.Date;

public class TimePickerPopupPanel extends JPanel {
    private JPanel rootPanel;
    private JBIntSpinner hourInput;
    private JBIntSpinner minuteInput;
    private JBIntSpinner secondInput;
    @Getter
    private CalendarPane calendarPane;

    public TimePickerPopupPanel() {
        super();
        $$$setupUI$$$();
        final GridLayoutManager layout = new GridLayoutManager(1, 1);
        this.setLayout(layout);
        this.add(this.rootPanel, new GridConstraints(0, 0, 1, 1, 0, GridConstraints.ALIGN_FILL, 3, 3, null, null, null, 0));
        this.initListeners();
    }

    public void updateDate(Date date) throws PropertyVetoException {
        final Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.setTime(date);
        this.hourInput.setValue(calendarInstance.get(Calendar.HOUR_OF_DAY));
        this.minuteInput.setValue(calendarInstance.get(Calendar.MINUTE));
        this.secondInput.setValue(calendarInstance.get(Calendar.SECOND));
        this.getCalendarPane().setDate(date);
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    private void initListeners() {
        final PropertyChangeListener propertyChangeListener = e -> {
            if ("date".equals(e.getPropertyName())) {
                final Calendar calendar = Calendar.getInstance();
                final Object oldDate = e.getOldValue();
                final Object newDate = e.getNewValue();
                if (newDate != null) {
                    calendar.setTime((Date) newDate);
                    calendar.set(Calendar.HOUR_OF_DAY, hourInput.getNumber());
                    calendar.set(Calendar.MINUTE, minuteInput.getNumber());
                    calendar.set(Calendar.SECOND, secondInput.getNumber());
                    this.firePropertyChange("date", oldDate, calendar.getTime());
                }
            }
        };
        this.calendarPane.addPropertyChangeListener(propertyChangeListener);
    }

    private void createUIComponents() {
        this.hourInput = new JBIntSpinner(0, 0, 23);
        this.minuteInput = new JBIntSpinner(0, 0, 59);
        this.secondInput = new JBIntSpinner(0, 0, 59);
    }
}
