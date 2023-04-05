package com.microsoft.azure.toolkit.intellij.monitor.view.right.filter.timerange;

import com.intellij.util.ui.JBUI;
import com.michaelbaranov.microba.calendar.DatePicker;
import com.michaelbaranov.microba.calendar.resource.Resource;
import com.michaelbaranov.microba.common.CommitEvent;
import com.michaelbaranov.microba.common.CommitListener;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.*;

public class TimePicker extends DatePicker {
    private JPopupMenu popup;
    private TimePickerPopupPanel popupPanel;
    private JFormattedTextField field;
    private JButton button;
    private ComponentListener componentListener;

    public TimePicker() {
        this(new Date(), 2, Locale.getDefault(), TimeZone.getDefault());
    }

    public TimePicker(Date initialDate, int dateStyle, Locale locale, TimeZone zone) {
        super(initialDate, dateStyle, locale, zone);
        this.initComponents();
    }

    public void showPopup() {
        showPopup(true);
    }

    public void hidePopup() {
        showPopup(false);
    }

    public void updateDate(Date date) throws PropertyVetoException {
        this.popupPanel.updateDate(date);
    }

    private void showPopup(boolean visible) {
        if (visible) {
            if (this.isKeepTime()) {
                try {
                    final JFormattedTextField.AbstractFormatter formatter = this.field.getFormatter();
                    final Date value = (Date)formatter.stringToValue(this.field.getText());
                    this.popupPanel.getCalendarPane().removePropertyChangeListener(this.componentListener);
                    this.popupPanel.updateDate(value);
                    this.popupPanel.getCalendarPane().addPropertyChangeListener(this.componentListener);
                } catch (final Exception ignored) {
                }
            }
            this.popup.show(this, 0, this.getHeight());
            this.popupPanel.requestFocus(false);
        } else {
            this.popup.setVisible(false);
        }
    }

    private void initComponents() {
        this.initPicker();
        this.initPop();
        this.revalidate();
        this.repaint();
        this.initListeners();
        this.peerDateChanged(this.getDate());
    }

    private void initListeners() {
        this.componentListener = new ComponentListener();
        this.button.addActionListener(this.componentListener);
        this.field.addPropertyChangeListener(this.componentListener);
        this.popupPanel.getCalendarPane().addPropertyChangeListener(this.componentListener);
        this.popupPanel.getCalendarPane().addCommitListener(this.componentListener);
        this.popupPanel.getCalendarPane().addActionListener(this.componentListener);
        this.popupPanel.addPropertyChangeListener(this.componentListener);
        this.addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                final Date newValue = (Date)evt.getNewValue();
                peerDateChanged(newValue);
            } else if ("focusLostBehavior".equals(evt.getPropertyName())) {
                field.setFocusLostBehavior(getFocusLostBehavior());
            } else if ("dateFormat".equals(evt.getPropertyName())) {
                field.setFormatterFactory(createFormatterFactory());
            } else {
                final Boolean value;
                if ("focusable".equals(evt.getPropertyName())) {
                    value = (Boolean) evt.getNewValue();
                    field.setFocusable(value);
                } else if ("enabled".equals(evt.getPropertyName())) {
                    value = (Boolean)evt.getNewValue();
                    field.setEnabled(value);
                    button.setEnabled(value);
                }
            }
        });
    }

    private void initPicker() {
        this.field = new JFormattedTextField(this.createFormatterFactory());
        this.field.setValue(this.getDate());
        this.field.setFocusLostBehavior(this.getFocusLostBehavior());
        this.field.setEditable(this.isFieldEditable());
        this.field.setToolTipText(this.getToolTipText());
        this.button = new JButton();
        this.button.setFocusable(false);
        this.button.setMargin(JBUI.emptyInsets());
        this.button.setToolTipText(this.getToolTipText());
        this.setSimpleLook();
        this.setLayout(new BorderLayout());
        this.add(this.field, "Center");
        this.add(this.button, "East");
    }

    private void initPop() {
        this.popupPanel = new TimePickerPopupPanel();
        this.popupPanel.getCalendarPane().setShowTodayButton(this.isShowTodayButton());
        this.popupPanel.getCalendarPane().setFocusLostBehavior(2);
        this.popupPanel.setFocusCycleRoot(true);
        this.popupPanel.setBorder(BorderFactory.createEmptyBorder(1, 3, 0, 3));
        this.popupPanel.getCalendarPane().setStripTime(false);
        this.popupPanel.getCalendarPane().setLocale(this.getLocale());
        this.popupPanel.getCalendarPane().setZone(this.getZone());
        this.popupPanel.setFocusable(this.isDropdownFocusable());
        this.popupPanel.getCalendarPane().setColorOverrideMap(this.getColorOverrideMap());
        this.popup = new JPopupMenu();
        this.popup.setLayout(new BorderLayout());
        this.popup.add(this.popupPanel, "Center");
        this.popup.setLightWeightPopupEnabled(true);
    }

    private DefaultFormatterFactory createFormatterFactory() {
        return new DefaultFormatterFactory(new DateFormatter(this.getDateFormat()));
    }

    private void setSimpleLook() {
        this.field.setBorder((new JTextField()).getBorder());
        this.button.setText("");
        this.button.setIcon(new ImageIcon(Resource.class.getResource("picker-16.png")));
    }

    private void peerDateChanged(Date newValue) {
        try {
            this.popupPanel.getCalendarPane().removePropertyChangeListener(this.componentListener);
            this.popupPanel.updateDate(newValue);
            this.popupPanel.getCalendarPane().addPropertyChangeListener(this.componentListener);
        } catch (final Exception ignored) {
        }
        this.field.removePropertyChangeListener(this.componentListener);
        this.field.setValue(newValue);
        this.field.addPropertyChangeListener(this.componentListener);
    }

    private class ComponentListener implements ActionListener, PropertyChangeListener, CommitListener {
        public ComponentListener() {
        }

        public void actionPerformed(ActionEvent e) {
            showPopup(e.getSource() != popupPanel.getCalendarPane());
        }

        public void propertyChange(PropertyChangeEvent evt) {
            Date fieldValue;
            if (evt.getSource() == popupPanel && "date".equals(evt.getPropertyName())) {
                showPopup(false);
                try {
                    final JFormattedTextField.AbstractFormatter formatter = field.getFormatter();
                    fieldValue = (Date)formatter.stringToValue(field.getText());
                } catch (final ParseException e) {
                    fieldValue = (Date)field.getValue();
                }
                if (fieldValue != null || evt.getNewValue() != null) {
                    if (isKeepTime() && fieldValue != null && evt.getNewValue() != null) {
                        final Calendar fieldCal = Calendar.getInstance(getZone(), getLocale());
                        fieldCal.setTime(fieldValue);
                        final Calendar valueCal = Calendar.getInstance(getZone(), getLocale());
                        valueCal.setTime((Date)evt.getNewValue());
                        fieldCal.set(Calendar.ERA, valueCal.get(Calendar.ERA));
                        fieldCal.set(Calendar.YEAR, valueCal.get(Calendar.YEAR));
                        fieldCal.set(Calendar.MONTH, valueCal.get(Calendar.MONTH));
                        fieldCal.set(Calendar.DATE, valueCal.get(Calendar.DATE));
                        fieldCal.set(Calendar.HOUR_OF_DAY, valueCal.get(Calendar.HOUR_OF_DAY));
                        fieldCal.set(Calendar.MINUTE, valueCal.get(Calendar.MINUTE));
                        fieldCal.set(Calendar.SECOND, valueCal.get(Calendar.SECOND));
                        field.setValue(fieldCal.getTime());
                    } else {
                        field.setValue(evt.getNewValue());
                    }
                }
            }
            if (evt.getSource() == field && "value".equals(evt.getPropertyName())) {
                fieldValue = (Date)field.getValue();
                try {
                    popupPanel.updateDate(fieldValue);
                } catch (final PropertyVetoException e) {
                    field.setValue(getDate());
                }
            }
        }

        public void commit(CommitEvent action) {
            showPopup(false);
            if (field.getValue() != null || popupPanel.getCalendarPane().getDate() != null) {
                field.setValue(popupPanel.getCalendarPane().getDate());
            }
        }

        public void revert(CommitEvent action) {
            showPopup(false);
        }
    }

}
