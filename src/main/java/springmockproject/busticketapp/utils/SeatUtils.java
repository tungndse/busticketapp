package springmockproject.busticketapp.utils;

import springmockproject.busticketapp.entity.Seat;

import java.util.ArrayList;
import java.util.List;

public class SeatUtils {

    public static List<List<Seat>> convertToMatrix(List<Seat> list) {
        List<List<Seat>> result = new ArrayList<>();
        int rowCount = list.size() % 4 != 0 ?
                list.size() / 4 + 1 : list.size() / 4;

        int index = 0;

        for (int i = 0; i < rowCount; i++) {
            List<Seat> row = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                row.add(list.get(index++));
            }

            result.add(row);
        }

        return result;
    }

}
