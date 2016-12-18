package jp.gr.java_conf.suhirotaka.android_noteapp_proto;

import android.provider.BaseColumns;

public final class SmallStepsContract {

    public SmallStepsContract() {}

    public static abstract class Goals implements BaseColumns {
        public static final String TABLE_NAME = "goals";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CONTAINING_TASK_IDS = "containing_task_ids";
        public static final String COLUMN_NAME_ID_AS_TASK = "id_as_task";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }

    public static abstract class Tasks implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DEADLINE = "deadline";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_BELONGING_GOAL_ID = "belonging_goal_id";
        public static final String COLUMN_NAME_ID_AS_GOAL = "id_as_goal";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }

}
