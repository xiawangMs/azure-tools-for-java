package com.microsoft.azure.toolkit.intellij.monitor.view.right.filter.timerange;

import com.intellij.ui.JBIntSpinner;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.michaelbaranov.microba.calendar.CalendarPane;
import lombok.Getter;

import javax.swing.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimePickerPopupPanel extends JPanel {
    private JPanel rootPanel;
    private JBIntSpinner hourInput;
    private JBIntSpinner minuteInput;
    private JBIntSpinner secondInput;
    @Getter
    private CalendarPane calendarPane;
    @Getter
    private Date date;
    @Getter
    private TimeZone zone;
    @Getter
    private Locale locale;

    public TimePickerPopupPanel() {
        super();
        $$$setupUI$$$();
        final GridLayoutManager layout = new GridLayoutManager(1, 1);
        this.setLayout(layout);
        this.add(this.rootPanel, new GridConstraints(0, 0, 1, 1, 0, GridConstraints.ALIGN_FILL, 3, 3, null, null, null, 0));
    }

    public void setDate(Date date) {
        final Date old = this.date;
        this.date = date;
        if (old != null || date != null) {
            this.firePropertyChange("date", old, date);
        }
    }

    public void setLocale(Locale locale) {
        final Locale old = this.getLocale();
        this.locale = locale;
        this.firePropertyChange("locale", old, this.getLocale());
    }

    public void setZone(TimeZone zone) {
        final TimeZone old = this.getZone();
        this.zone = zone;
        this.firePropertyChange("zone", old, this.getZone());
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    private void createUIComponents() {
        this.hourInput = new JBIntSpinner(0, 0, 23);
        this.minuteInput = new JBIntSpinner(0, 0, 59);
        this.secondInput = new JBIntSpinner(0, 0, 59);
    }
}
