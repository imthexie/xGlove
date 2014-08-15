void setup() {
    Serial1.begin(15200);
    pinMode(D0, INPUT);
    pinMode(D1, INPUT);
}

void loop() {
    
    //Moves mouse left and right based on pin values
    if(digitalRead(D0) == HIGH && digitalRead(D1) == LOW) {
        Serial1.println("Data,2,0,1");
    }
    else if(digitalRead(D0) == LOW && digitalRead(D1) == HIGH) {
        Serial1.println("Data,-2,0,1");
    } else {
        Serial1.println("Data,0,0,0");
    }
    delay(50);
}

