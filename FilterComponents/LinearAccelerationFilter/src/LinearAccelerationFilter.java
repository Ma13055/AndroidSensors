// -*- Java -*-
/*!
 * @file LinearAccelerationFilter.java
 * @date $Date$
 *
 * @author Masaru Tatekawa
 * ma13055@shibaura-it.ac.jp
 *
 * $Id$
 */

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;
import jp.go.aist.rtm.RTC.RtcNewFunc;
import jp.go.aist.rtm.RTC.RegisterModuleFunc;
import jp.go.aist.rtm.RTC.util.Properties;

/*!
 * @class LinearAccelerationFilter
 * @brief Filtering of the LinearAccelerationSensor information
 */
public class LinearAccelerationFilter implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "LinearAccelerationFilter",
    	    "type_name",         "LinearAccelerationFilter",
    	    "description",       "Filtering of the LinearAccelerationSensor information",
    	    "version",           "1.0.0",
    	    "vendor",            "Masatu Tatekawa",
    	    "category",          "Filter",
    	    "activity_type",     "UNIQUE",
    	    "max_instance",      "3",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.ImportTextName", "null",
            "conf.default.ChangedOfBaseline", "0",
            "conf.default.VariableToRounding", "1",
            "conf.default.PitchTime", "1000",
            // Widget
            "conf.__widget__.ImportTextName", "text",
            "conf.__widget__.ChangedOfBaseline", "radio",
            "conf.__widget__.VariableToRounding", "text",
            "conf.__widget__.PitchTime", "text",
            // Constraints
            "conf.__constraints__.ChangedOfBaseline", "(0,1)",
            "conf.__constraints__.VariableToRounding", "x>0",
            "conf.__constraints__.PitchTime", "x>=0",
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new LinearAccelerationFilterImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new LinearAccelerationFilter(), new LinearAccelerationFilter());
    }
}
