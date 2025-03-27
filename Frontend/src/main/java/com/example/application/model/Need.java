package com.example.application.model;


import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.UrgencyLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Need {
    private int id;

    @Setter
    private String description;

    @Setter
    private UrgencyLevel urgency;

    @Setter
    private NeedType needType;

    @Setter
    private GPSCoordinates location;

    @Setter
    private Catastrophe catastrophe;

    @Setter
    private Task task;


    public Need(String description, UrgencyLevel urgency, NeedType needType, GPSCoordinates location, Catastrophe catastrophe) {
        this.description = description;
        this.urgency = urgency;
        this.needType = needType;
        this.location = location;
        this.catastrophe = catastrophe;
    }
}
