package com.meilinger.tj.admin;

import java.util.Date;

public interface DayViewListener{
    void onActiveChanged(int dayOfWeek, boolean isActive);
    void onStartTimeChanged(int dayOfWeek, Date startTime);
    void onEndTimeChanged(int dayOfWeek, Date endTime);
}
