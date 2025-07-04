/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.ml.jobs;

import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;

import java.io.IOException;
import java.time.Instant;

import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.core.xcontent.XContentParserUtils;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.schedule.Schedule;
import org.opensearch.jobscheduler.spi.schedule.ScheduleParser;
import org.opensearch.ml.utils.ParseUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Setter
@Log4j2
public class MLJobParameter implements ScheduledJobParameter {
    public static final String NAME_FIELD = "name";
    public static final String ENABLED_FILED = "enabled";
    public static final String LAST_UPDATE_TIME_FIELD = "last_update_time";
    public static final String LAST_UPDATE_TIME_FIELD_READABLE = "last_update_time_field";
    public static final String SCHEDULE_FIELD = "schedule";
    public static final String ENABLED_TIME_FILED = "enabled_time";
    public static final String ENABLED_TIME_FILED_READABLE = "enabled_time_field";
    public static final String LOCK_DURATION_SECONDS = "lock_duration_seconds";
    public static final String JITTER = "jitter";
    public static final String TYPE = "type";

    private String jobName;
    private Instant lastUpdateTime;
    private Instant enabledTime;
    private boolean isEnabled;
    private Schedule schedule;
    private Long lockDurationSeconds;
    private Double jitter;

    @Getter
    private MLJobType jobType;

    public MLJobParameter() {}

    public MLJobParameter(String name, Schedule schedule, Long lockDurationSeconds, Double jitter, MLJobType jobType) {
        this.jobName = name;
        this.schedule = schedule;
        this.lockDurationSeconds = lockDurationSeconds;
        this.jitter = jitter;

        Instant now = Instant.now();
        this.isEnabled = true;
        this.enabledTime = now;
        this.lastUpdateTime = now;
        this.jobType = jobType;
    }

    @Override
    public String getName() {
        return this.jobName;
    }

    @Override
    public Instant getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Override
    public Instant getEnabledTime() {
        return this.enabledTime;
    }

    @Override
    public Schedule getSchedule() {
        return this.schedule;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public Long getLockDurationSeconds() {
        return this.lockDurationSeconds;
    }

    @Override
    public Double getJitter() {
        return jitter;
    }

    public static MLJobParameter parse(XContentParser parser) throws IOException {
        MLJobParameter jobParameter = new MLJobParameter();
        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);

        while (!parser.nextToken().equals(XContentParser.Token.END_OBJECT)) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case MLJobParameter.NAME_FIELD:
                    jobParameter.setJobName(parser.text());
                    break;
                case MLJobParameter.ENABLED_FILED:
                    jobParameter.setEnabled(parser.booleanValue());
                    break;
                case MLJobParameter.ENABLED_TIME_FILED:
                    jobParameter.setEnabledTime(ParseUtils.toInstant(parser));
                    break;
                case MLJobParameter.LAST_UPDATE_TIME_FIELD:
                    jobParameter.setLastUpdateTime(ParseUtils.toInstant(parser));
                    break;
                case MLJobParameter.SCHEDULE_FIELD:
                    jobParameter.setSchedule(ScheduleParser.parse(parser));
                    break;
                case MLJobParameter.LOCK_DURATION_SECONDS:
                    jobParameter.setLockDurationSeconds(parser.longValue());
                    break;
                case MLJobParameter.JITTER:
                    jobParameter.setJitter(parser.doubleValue());
                    break;
                case MLJobParameter.TYPE:
                    String type = parser.text();
                    jobParameter.setJobType(MLJobType.valueOf(type));
                    break;
                default:
                    XContentParserUtils.throwUnknownToken(parser.currentToken(), parser.getTokenLocation());
            }
        }

        return jobParameter;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(NAME_FIELD, this.jobName).field(ENABLED_FILED, this.isEnabled).field(SCHEDULE_FIELD, this.schedule);
        if (this.enabledTime != null) {
            builder.timeField(ENABLED_TIME_FILED, ENABLED_TIME_FILED_READABLE, this.enabledTime.toEpochMilli());
        }
        if (this.lastUpdateTime != null) {
            builder.timeField(LAST_UPDATE_TIME_FIELD, LAST_UPDATE_TIME_FIELD_READABLE, this.lastUpdateTime.toEpochMilli());
        }
        if (this.lockDurationSeconds != null) {
            builder.field(LOCK_DURATION_SECONDS, this.lockDurationSeconds);
        }
        if (this.jitter != null) {
            builder.field(JITTER, this.jitter);
        }
        if (this.jobType != null) {
            builder.field(TYPE, this.jobType.toString());
        }
        builder.endObject();
        return builder;
    }
}
