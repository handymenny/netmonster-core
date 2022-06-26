package cz.mroczis.netmonster.core.feature.postprocess

/**
 * Represents unique identifier of Cell postprocessors
 */
enum class CellPostprocessor {
    /**
     *  Corresponds to [CdmaPlmnPostprocessor]
     */
    CDMA_PLMN_POSTPROCESSOR,

    /**
     *  Corresponds to [CellBandwidthPostprocessor]
     */
    CELL_BANDWIDTH_POSTPROCESSOR,

    /**
     *  Corresponds to [InvalidCellsPostprocessor]
     */
    INVALID_CELLS_POSTPROCESSOR,

    /**
     *  Corresponds to [MocnNetworkPostprocessor]
     */
    MOCN_NETWORK_POSTPROCESSOR,

    /**
     *  Corresponds to [PhysicalChannelPostprocessor]
     */
    PHYSICAL_CHANNEL_POSTPROCESSOR,

    /**
     *  Corresponds to [PLMN_POSTPROCESSOR]
     */
    PLMN_POSTPROCESSOR,

    /**
     *  Corresponds to [PrimaryCellPostprocessor]
     */
    PRIMARY_CELL_POSTPROCESSOR,

    /**
     *  Corresponds to [SamsungEndiannessPostprocessor]
     */
    SAMSUNG_ENDIANNESS_POSTPROCESSOR,

    /**
     *  Corresponds to [SamsungInvalidValuesPostprocessor]
     */
    SAMSUNG_INVALID_VALUES_POSTPROCESSOR,

    /**
     *  Corresponds to [SignalStrengthPostprocessor]
     */
    SIGNAL_STRENGTH_POSTPROCESSOR,

    /**
     *  Corresponds to [SubDuplicitiesPostprocessor]
     */
    SUB_DUPLICITIES_POSTPROCESSOR,

    /**
     *  Corresponds to [InvalidSecondaryCellsPostprocessor]
     */
    INVALID_SECONDARY_CELLS_POSTPROCESSOR
}