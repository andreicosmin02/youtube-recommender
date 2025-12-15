import React, { createContext, useState } from 'react';

// The ESLint rule warns that this is not a Component. 
// We disable it here because exporting the Context object is necessary.
// eslint-disable-next-line react-refresh/only-export-components
export const RecommendationContext = createContext();

export const RecommendationProvider = ({ children }) => {
    // State for Home Page persistence
    const [homeQuery, setHomeQuery] = useState('');
    const [homeData, setHomeData] = useState(null); // Stores { aiResponse, videos }
    const [loading, setLoading] = useState(false);

    return (
        <RecommendationContext.Provider value={{
            homeQuery,
            setHomeQuery,
            homeData,
            setHomeData,
            loading,
            setLoading
        }}>
            {children}
        </RecommendationContext.Provider>
    );
};