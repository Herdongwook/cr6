package Controller;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.*;
import common.ClassManager;
import Model.LectureDTO;
import Model.TimeDTO;
import View.LectureListView;
import common.Constants;
import org.json.simple.JSONObject;

public class LectureListController implements ActionListener {

    LectureListView LLV;
    ArrayList<LectureDTO> myLecture;

    public LectureListController(LectureListView LLV) {
        this.LLV = LLV;
    }

    public CellRenderer connectCellRenderer() {
        return new CellRenderer();
    }

    public void setScore() {
        LLV.setScore(Integer.toString(countScore()));
    }

    private int countScore() {
        int sums = 0;
        for(int i = 0 ; i < myLecture.size(); i++)
            sums += (int)myLecture.get(i).getScore();
        return sums;
    }

    private boolean compareData(Object[] inserted, boolean isPopUp) {
        for(int i = 0 ; i < myLecture.size();i++) {
            if(myLecture.get(i).getCourseNum().equals(inserted[3])) {
                if(isPopUp)
                    JOptionPane.showMessageDialog(null, "이미 신청한 과목입니다.");
                return false;
            }
            if(myLecture.get(i).getTime() != null && inserted[9] != null && isClassOverLap(myLecture.get(i).getTime(),inserted[9].toString())) {
                if(isPopUp)
                    JOptionPane.showMessageDialog(null, "시간이 겹치는 과목이 있습니다.");
                return false;
            }
            if(countScore() + Double.parseDouble(inserted[7].toString()) > 24 && isPopUp) {
                JOptionPane.showMessageDialog(null, "24학점 이상 신청할 수 없습니다..");
                return false;
            }
        }
        return true;
    }

    private boolean isClassOverLap(String data1,String data2){
        ArrayList<TimeDTO> times1 = getTimeList(data2);
        ArrayList<TimeDTO> times2 = getTimeList(data1);
        for (int index1 = 0; index1 < times1.size(); index1++){
            TimeDTO time1 = times1.get(index1);
            for (int index2 = 0; index2 < times2.size(); index2++){
                TimeDTO time2 = times2.get(index2);
                boolean isDayOverLap = time1.Day.equals(time2.Day);
                if (isDayOverLap && isTimeOverlap(time1, time2)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTimeOverlap(TimeDTO class1, TimeDTO class2){
        boolean isStartTime = true;
        int class1StartTime = class1.ToMinute(isStartTime);
        int class2StartTime = class2.ToMinute(isStartTime);

        if(class1StartTime < class2StartTime) {
            int class1EndTime = class1.ToMinute(!isStartTime);
            return (class1StartTime <= class2StartTime) && (class2StartTime < class1EndTime);
        }
        int class2EndTime = class2.ToMinute(!isStartTime);
        return (class2StartTime <= class1StartTime) && (class1StartTime < class2EndTime);
    }

    private ArrayList<TimeDTO> getTimeList(String timeString){
        ArrayList<TimeDTO> times = new ArrayList<TimeDTO>();
        String[] splitComma = timeString.split(",");
        int timeIndex = 0;
        for(String text : splitComma){
            String[] splitSpace = text.split(" ");
            timeIndex = splitDay(times, splitSpace, timeIndex);
        }
        return times;
    }

    private int splitDay(ArrayList<TimeDTO> times, String[] splitSpace, int timeIndex){
        for(String word : splitSpace){
            if (word.length() == 0)
                continue;
            if (Pattern.matches("^[가-힣]*$", word)){
                TimeDTO time = new TimeDTO();
                time.Day = word;
                times.add(time);
            }
            else{
                timeIndex = splitTime(times, word, timeIndex);
            }
        }
        return timeIndex;
    }

    private int splitTime(ArrayList<TimeDTO> times, String word, int timeIndex){
        String[] splitTime = word.split("~");

        for (int index = timeIndex; index < times.size(); index++){
            TimeDTO time = times.get(index);
            String[] startTime = splitTime[0].split(":");
            times.get(index).startHour = Integer.parseInt(startTime[0]);
            time.startMinute = Integer.parseInt(startTime[1]);

            String[] endTime = splitTime[1].split(":");
            time.endHour = Integer.parseInt(endTime[0]);
            time.endMinute = Integer.parseInt(endTime[1]);
        }
        return times.size();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String operator = ((JButton)e.getSource()).getText();
        switch (operator){
            case Constants.APPLY_TXT:
                if(isGoodinSearchTable()) {
                    Object news [] = new Object[12];
                    for(int i = 0 ; i < 12; i++)
                        news[i] = LLV.getSearchListDTM().getValueAt(LLV.getSearchListTable().getSelectedRow(), i);
                    if(!compareData(news,true))
                        return;
                    LLV.getMyLectureDTM().addRow(news);
                    ClassManager.getInstance().getDAO().applyLecture(ClassManager.getInstance().getMainMenuView().getUser(),new LectureDTO(news));
                    myLecture = ClassManager.getInstance().getDAO().getMyLecture(ClassManager.getInstance().getMainMenuView().getUser());
                    LLV.changeMyLectureDTM(myLecture);
                    setScore();
                    LLV.getSearchListTable().repaint();
                }
                break;
            case Constants.CANCEL_TXT:
                if(isGoodinMyTable()) {
                    for(int i = 0; i < myLecture.size(); i++)
                        if(myLecture.get(i).getCourseNum() == LLV.getMyLectureDTM().getValueAt(LLV.getMyLectureTable().getSelectedRow(),3)) {
                            ClassManager.getInstance().getDAO().cancelLecture(ClassManager.getInstance().getMainMenuView().getUser(),myLecture.get(i));
                            myLecture.remove(i);
                            LLV.getMyLectureDTM().removeRow(LLV.getMyLectureTable().getSelectedRow());
                        }
                    setScore();
                    LLV.getSearchListTable().repaint();
                }
                break;
            case Constants.EXIT_TXT:
                ClassManager.getInstance().getMain().comeToMain();
                break;
            case Constants.REFRESH_TXT:
                LLV.changeSearchDTM(ClassManager.getInstance().getDAO().getFilterLecture(new JSONObject()));
                break;
        }
    }

    private boolean isGoodinSearchTable(){
        if(LLV.getSearchListTable().getSelectedRow() >= 0
                && LLV.getSearchListTable().getSelectedRow() < LLV.getSearchListTable().getRowCount())
            return true;
        return false;
    }
    private boolean isGoodinMyTable(){
        if(LLV.getMyLectureTable().getSelectedRow() >= 0
                && LLV.getMyLectureTable().getSelectedRow() < LLV.getMyLectureTable().getRowCount())
            return true;
        return false;
    }
    public void setMyLecture(ArrayList<LectureDTO> list){
        this.myLecture = list;
    }

    public class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if(table == LLV.getMyLectureTable())
                return cell;
            if(col == 5) {
                Object news [] = new Object[12];
                for(int j = 0 ; j < 12; j++)
                    news[j] = LLV.getSearchListDTM().getValueAt(row, j);
                if(compareData(news,false))
                    cell.setForeground(Color.black);
                else
                    cell.setForeground(Color.red);
            }
            else
                cell.setForeground(Color.black);

            return cell;
        }
    }
}
