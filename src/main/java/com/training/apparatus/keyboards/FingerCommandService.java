package com.training.apparatus.keyboards;

import com.training.apparatus.translation.TranslationProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

import static com.training.apparatus.keyboards.Hand.*;
import static com.training.apparatus.keyboards.Finger.*;
import static com.training.apparatus.keyboards.Position.*;


/**
 * @author Kulikov Denis
 * @since 13.10.2022
 */
@Service
public class FingerCommandService {

    private final Map<Locale, Map<Character, Command>> localeMap = new HashMap<>();


    private final Map<Character, Command> russianCommands;

    public static final char ABSENT = (char) 0;

    {
        russianCommands = new HashMap<>();

        Map<List<Character>, GenCommand> map = new HashMap<>();
        map.put(List.of('5', 'е' ,'п', 'и'), new GenCommand(LeftHand, First,false, Right));
        map.put(List.of('%', 'Е' ,'П', 'И'), new GenCommand(LeftHand, First,true, RightTwice));

        map.put(List.of('4', 'к' ,'а', 'м'), new GenCommand(LeftHand, First, false));
        map.put(List.of('$', 'К' ,'А', 'М'), new GenCommand(LeftHand, First, true));

        map.put(List.of('3', 'у' ,'в', 'с'), new GenCommand(LeftHand, Second,false));
        map.put(List.of('№', 'У' ,'В', 'С'), new GenCommand(LeftHand, Second,true));

        map.put(List.of('2', 'ц' ,'ы', 'ч'), new GenCommand(LeftHand, Third,false));
        map.put(List.of('"', 'Ц' ,'Ы', 'Ч'), new GenCommand(LeftHand, Third,true));

        map.put(List.of('1', 'й' ,'ф', 'я'), new GenCommand(LeftHand, Fourth, false));
        map.put(List.of('!', 'Й' ,'Ф', 'Я'), new GenCommand(LeftHand, Fourth,true));

//        map.put(List.of('ё', ABSENT ,ABSENT, '\\'), new GenCommand(LeftHand, Fourth, false, Left));
//        map.put(List.of('~', ABSENT ,ABSENT, '|'), new GenCommand(LeftHand, Fourth,true, Left));


        map.put(List.of('6', 'н' ,'р', 'т'), new GenCommand(RightHand, First,false, Left));
        map.put(List.of(':', 'Н' ,'Р', 'Т'), new GenCommand(RightHand, First,true, Left));

        map.put(List.of('7', 'г' ,'о', 'ь'), new GenCommand(RightHand, First,false));
        map.put(List.of('?', 'Г' ,'О', 'Ь'), new GenCommand(RightHand, First,true));

        map.put(List.of('8', 'ш' ,'л', 'с'), new GenCommand(RightHand, Second, false));
        map.put(List.of('*', 'Ш' ,'Л', 'С'), new GenCommand(RightHand, Second, true));

        map.put(List.of('9', 'щ' ,'д', 'ю'), new GenCommand(RightHand, Third, false));
        map.put(List.of('(', 'Щ' ,'Д', 'Ю'), new GenCommand(RightHand, Third,true));

        map.put(List.of('0', 'з' ,'ж', '.'), new GenCommand(RightHand, Fourth, false));
        map.put(List.of(')', 'З' ,'Ж', ','), new GenCommand(RightHand, Fourth, true));

        map.put(List.of('-', 'х' , 'э', ABSENT), new GenCommand(RightHand, Fourth, false, Right));
        map.put(List.of('_', 'Х' , 'Э', ABSENT), new GenCommand(RightHand, Fourth, true, Right));

        map.put(List.of('=', 'ъ' , '\\', ABSENT), new GenCommand(RightHand, Fourth, false, RightTwice));
        map.put(List.of('+', 'Ъ', '|', ABSENT), new GenCommand(RightHand, Fourth, true, RightTwice));

        map.put(List.of(ABSENT, ABSENT, ' ', ABSENT), new GenCommand(RightHand, Zero, false));

        List<Position> positions = List.of(UpTwice, Up, InPlace, Down);
        map.forEach((characters, genCommand) -> {

            for (int i = 0; i < characters.size(); i++) {
                Character key = characters.get(i);
                if (key != ABSENT) {
                    if (genCommand.additionalPosition != null) {
                        russianCommands.put(key, new Command(genCommand.shift, genCommand.hand, genCommand.finger, genCommand.additionalPosition, positions.get(i)));
                    } else {
                        russianCommands.put(key, new Command(genCommand.shift, genCommand.hand, genCommand.finger, positions.get(i)));
                    }
                }
            }
        });

        localeMap.put(TranslationProvider.LOCALE_RU, russianCommands);

    }
    public Map<Character, Command> getCommandMapper(Locale locale) {
        return localeMap.getOrDefault(locale, russianCommands);
    }

    record GenCommand(Hand hand, Finger finger, boolean shift, Position additionalPosition){
        GenCommand(Hand hand, Finger finger, boolean shift) {
            this(hand, finger, shift, null);
        }
    }
}
