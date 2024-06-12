package View;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.BorderLayout;

import Controller.EnrollmentControllar;
import Controller.EnrollmentController;

public class EnrollmentView extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList<String> courseList;
    private JButton enrollButton, cancelButton;

    public EnrollmentView() {
        setLayout(new BorderLayout());
        courseList = new JList<>(new String[] {"Course 1", "Course 2", "Course 3"});
        enrollButton = new JButton("Enroll");
        cancelButton = new JButton("Cancel");

        add(new JScrollPane(courseList), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(enrollButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        enrollButton.addActionListener(new EnrollmentController(this));
        cancelButton.addActionListener(new EnrollmentController(this));

    }

    public String getSelectedCourse() {
        return courseList.getSelectedValue();
    }

    public boolean isEnrollButton(Object obj) {
        return obj == enrollButton;
    }

    public boolean isCancelButton(Object obj) {
        return obj == cancelButton;
    }
}
